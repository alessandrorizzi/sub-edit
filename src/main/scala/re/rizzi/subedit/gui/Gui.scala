/**
  * Copyright (C) 2017  Alessandro M. Rizzi <github@rizzi.re>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
package re.rizzi.subedit.gui

import java.awt.event._
import java.awt.{BorderLayout, Frame}
import java.io.File
import java.util.Locale
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import re.rizzi.subedit.substitles.SubtitleView
import re.rizzi.subedit.utilities.{Localization, MenuItem}
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent

import scala.util.Try

class Gui {

  private val videoExtensions =
    Array("3gp", "asf", "wmv", "au", "avi", "flv", "mov", "mp4", "ogm", "ogg", "mkv", "mka", "ts", "mpg", "mp3", "mp2",
      "nsc", "nsv", "nut", "ra", "ram", "rm", "rv", "rmbv", "a52", "dts", "aac", "flac", "dv", "vid", "tta", "tac",
      "ty", "wav", "dts", "xa");

  private val frame = new JFrame();

  private val locale = Locale.getDefault();

  private val localization = Localization(locale);

  private val settings = new SettingsHandler(localization);
  Try(UIManager.setLookAndFeel(settings.lookAndFeel.getClassName));

  private val subtitleView = new SubtitleView(settings, 5);

  private val mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

  private var isSpacePressed = false;

  private val icon : ImageIcon = new ImageIcon(getClass.getResource("/icon.png"));

  private def tr(label : Symbol) : String = localization.tr(label);

  private def updateTitle() : Unit = {
    val changed = if(subtitleView.isChanged) "*" else "";
    val title = changed + subtitleView.currentFilename.map(_ + " - ").getOrElse("") + tr('appTitle);
    frame.setTitle(title);
  }

  private def toMenu(text : String) : (String, Option[Int]) = {
    var ampersand = false;
    var mnemonic : Option[Int] = None;

    def setMnemonic(character : Int) : Unit =
      if(character == ' ')
        mnemonic = Some(32);
      else if(character >= 48 && character <= 57 || character >= 65 && character <= 90)
        mnemonic = Some(character);

    val result = text.flatMap{
      case '&' if ampersand => ampersand = false; Some('&');
      case '&' => ampersand = true; None;
      case any => if(ampersand) setMnemonic(any.toUpper); ampersand = false; Some(any);
    }
    (result, mnemonic);
  }

  private def createMenu(text : String) : JMenu = {
    val (value, mnemonic) = toMenu(text);
    val result = new JMenu(value);
    mnemonic match {
      case Some(m) => result.setMnemonic(m);
      case None =>
    }
    result;
  }

  private def addMenuItem(label : Symbol, menu : JMenu) : MenuItem = {
    val (value, mnemonic) = toMenu(tr(label));
    val result = new MenuItem(label, value);
    menu.add(result);

    mnemonic match {
      case Some(m) => result.setMnemonic(m);
      case None =>
    }
    result;
  }

  private def getFileHelper(title : Symbol, fileDescription : Symbol,
                            extensions : String*) = {
    val fileOpen = new JFileChooser();
    fileOpen.setDialogTitle(tr(title));
    settings.lastUsedDirectory.foreach(fileOpen.setCurrentDirectory);
    val filter = new FileNameExtensionFilter(tr(fileDescription), extensions : _*);
    fileOpen.setFileFilter(filter);
    val returnVal = fileOpen.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      settings.updateLastUsedDirectory(fileOpen.getSelectedFile.getParent);
      Some(fileOpen.getSelectedFile.getPath);
    }
    else
      None;
  }

  private def associatedVideoFiles(subtitleFile : String) : Seq[String] = {
    val baseName = subtitleFile.reverse.dropWhile(_ != '.').reverse;
    videoExtensions.map(baseName + _).filter(new File(_).exists()).toSeq;
  }

  private def open() : Unit = {
    if(checkUnsavedChanges())
      getFileHelper('openSubtitleFile, 'subtitleFile, "srt").foreach{
        sub =>
          associatedVideoFiles(sub).headOption
            .orElse(getFileHelper('openVideoFile, 'videoFile, videoExtensions : _*)).foreach{
            videoFile =>
              mediaPlayerComponent.getMediaPlayer.stop();
              mediaPlayerComponent.getMediaPlayer.prepareMedia(videoFile);
              subtitleView.attach(sub) match {
                case Some(exception) =>
                  println(exception);
                  mediaPlayerComponent.getMediaPlayer.stop();
                  JOptionPane.showMessageDialog(frame, tr('invalidSubtitleFile));
                case None =>
              }
              subtitleView.update(true);
          }
      }
  }

  private def saveAs() : Unit = {
    def overwrite() : Boolean =
      JOptionPane.showConfirmDialog(frame, tr('overwriteDialogMessage), tr('overwriteDialogTitle),
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

    if(!subtitleView.isOpen)
      JOptionPane.showMessageDialog(frame, tr('subtitleFileNotOpen));
    else
      getFileHelper('openSubtitleFile, 'subtitleFile, "srt").foreach{
        filename =>
          if(!new File(filename).exists() || overwrite()){
            subtitleView.setFilename(filename);
            subtitleView.save();
          }
      }
  }

  private def showHelp() : Unit =
    JOptionPane.showMessageDialog(frame, tr('helpDialogMessage), tr('helpDialogTitle), JOptionPane.INFORMATION_MESSAGE);

  private object MenuListener extends ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit =
      actionEvent.getSource.asInstanceOf[MenuItem].label match {
        case 'openMenu =>
          open();
          updateTitle();
        case 'saveMenu =>
          subtitleView.save();
          updateTitle();
        case 'saveAsMenu =>
          saveAs();
          updateTitle();
        case 'exitMenu => frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        case 'settingsMenu => settings.show(frame);
        case 'helpMenu => showHelp();
        case 'aboutMenu => new AboutDialog(localization, frame);
      }
  }

  private def checkUnsavedChanges() : Boolean = {
    if(subtitleView.isChanged)
      JOptionPane.showConfirmDialog(frame, tr('unsavedChangesDialogMessage), tr('unsavedChangesDialogTitle),
        JOptionPane.YES_NO_CANCEL_OPTION) match {
        case JOptionPane.CANCEL_OPTION => false;
        case JOptionPane.YES_OPTION =>
          subtitleView.save();
          true;
        case JOptionPane.NO_OPTION =>
          true;
      }
    else
      true;
  }

  private object GuiWindowListener extends WindowAdapter {
    override def windowClosing(windowEvent: WindowEvent): Unit = {
      if(checkUnsavedChanges()) {
        mediaPlayerComponent.getMediaPlayer.stop();
        frame.setVisible(false);
        frame.dispose();
        System.exit(0);
      }
    }
  }

  private object KeyboardHandler extends KeyAdapter {

    override def keyPressed(keyEvent: KeyEvent): Unit = {
      keyEvent.getKeyChar match {
        case 'x' => subtitleView.skipCurrent();
        case 'z' => subtitleView.previous();
        case 'q' => subtitleView.previous();
          mediaPlayerComponent.getMediaPlayer.setTime(subtitleView.currentStartTime);
        case 'w' => subtitleView.skipCurrent();
          mediaPlayerComponent.getMediaPlayer.setTime(subtitleView.currentStartTime);
        case 'p' =>
          if(isSpacePressed){
            subtitleView.releaseSpace(mediaPlayerComponent.getMediaPlayer.getTime);
            isSpacePressed = false;
          }
          if(!mediaPlayerComponent.getMediaPlayer.isPlaying)
            mediaPlayerComponent.getMediaPlayer.play();
          else
            mediaPlayerComponent.getMediaPlayer.pause();
        case 'm' => mediaPlayerComponent.getMediaPlayer.skip(10000);
        case 'n' => mediaPlayerComponent.getMediaPlayer.skip(-10000);
        case _ =>
      }

      if(mediaPlayerComponent.getMediaPlayer.isPlaying && keyEvent.getKeyCode == 32 && !isSpacePressed) {
        subtitleView.pressSpace(mediaPlayerComponent.getMediaPlayer.getTime);
        isSpacePressed = true;
        updateTitle();
      }
    }

    override def keyReleased(keyEvent: KeyEvent): Unit = {
      if(keyEvent.getKeyCode == 32) {
        subtitleView.releaseSpace(mediaPlayerComponent.getMediaPlayer.getTime);
        isSpacePressed = false;
        updateTitle();
      }
    }

  }

  private def init() : Unit = {
    val layout = new BorderLayout();
    frame.setLayout(layout)

    val menuBar = new JMenuBar();
    val fileMenu = createMenu(tr('fileMenu));
    addMenuItem('openMenu, fileMenu).addActionListener(MenuListener);
    fileMenu.addSeparator();
    addMenuItem('saveMenu, fileMenu).addActionListener(MenuListener);
    addMenuItem('saveAsMenu, fileMenu).addActionListener(MenuListener);
    fileMenu.addSeparator();
    addMenuItem('exitMenu, fileMenu).addActionListener(MenuListener);
    menuBar.add(fileMenu);

    val editMenu = createMenu(tr('editMenu));
    addMenuItem('settingsMenu, editMenu).addActionListener(MenuListener);
    menuBar.add(editMenu);

    val helpMenu = createMenu(tr('helpMenu));
    addMenuItem('helpMenu, helpMenu).addActionListener(MenuListener);
    addMenuItem('aboutMenu, helpMenu).addActionListener(MenuListener);
    menuBar.add(helpMenu);



    frame.setJMenuBar(menuBar);

    frame.add(mediaPlayerComponent, BorderLayout.CENTER);
    frame.add(subtitleView.panel, BorderLayout.PAGE_END);
    frame.setLocation(0, 0);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    frame.setIconImage(icon.getImage);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(GuiWindowListener);
    frame.setSize(800, 600);
    frame.setVisible(true)
    subtitleView.update(true);

    frame.addKeyListener(KeyboardHandler);
    updateTitle();
  }

}

object Gui {

  def start() : Unit =
    SwingUtilities.invokeLater(() => new Gui().init());

}