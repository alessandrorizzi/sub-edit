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
package re.rizzi.subedit.substitles

import java.awt.{Color, GridBagConstraints, GridBagLayout}
import javax.swing.{JLabel, JPanel}

import re.rizzi.subedit.gui.SettingsHandler

import scala.util.{Failure, Success}

sealed class SubtitleView(settings : SettingsHandler, nextSubtitles : Int) {

  private var handler : SubtitleHandler = NullSubtitleHandler;

  private var filename : Option[String] = None;

  private var changed = false;

  private val current = new JLabel(" ");

  private val nextSlots = new Array[JLabel](nextSubtitles);

  val panel = new JPanel();

  private def colorToHTML(color : Color) : String = {
    "#%02x%02x%02x".format(color.getRed, color.getGreen, color.getBlue);
  }

  def isChanged : Boolean = changed;

  def currentFilename : Option[String] = filename;

  def currentStartTime: Long = handler.currentStartTime;

  def isOpen : Boolean = filename.nonEmpty;

  def setFilename(filename : String) : Unit = this.filename = Some(filename);

  def attach(subtitleFilePath : String) : Option[Throwable] = {
    filename = None;
    changed = false;
    SubtitleHandler(subtitleFilePath) match {
      case Success(subtitleFile) =>
        handler = subtitleFile;
        filename = Some(subtitleFilePath);
        None;
      case Failure(exception) => Some(exception);
    }
  }

  def update(showCurrent : Boolean) : Unit = {
    val next = handler.next(nextSubtitles);
    val width = panel.getWidth;
    next.zipWithIndex.foreach(x => nextSlots(x._2).
      setText("<html><div style=\\\"width:" + width + "px;\\\"><font color='" + colorToHTML(settings.subtitleColor) +
        "' size=10>" + x._1 + "&nbsp</font></div></html>"));
    if(showCurrent)
      current.setText("<html><div style=\\\"width:" + width + "px;\\\"><font color='" +
        colorToHTML(settings.highlightedSubtitleColor) + "' size=10>" + handler.current + "</font></div></html>");
    else
      current.setText("<html><div style=\\\"width:" + width +
        "px;\\\"><font color='red' size=10>&nbsp</font></div></html>");
  }

  def skipCurrent() : Unit = {
    handler.advance();
    update(false);
  }

  def previous() : Unit = {
    handler.back();
    update(false);
  }

  def pressSpace(time : Long) : Unit = {
    changed = true;
    handler.advance();
    update(true);
    handler.setBeginTime(time - settings.beginReactionTime);
  }

  def releaseSpace(time : Long) : Unit = {
    changed = true;
    update(false);
    handler.setEndTime(time - settings.endReactionTime);
  }

  def save() : Unit = {
    filename.foreach(handler.save);
    changed = false;
  }

  private def getConstraints(i: Int) : GridBagConstraints = {
    val result = new GridBagConstraints();
    result.gridx = 0;
    result.gridy = i + 1;
    result;
  }

  Array.range(0, nextSubtitles).foreach(i => nextSlots(i) = new JLabel(" "));
  panel.setLayout(new GridBagLayout());
  panel.add(current);
  Array.range(0, nextSubtitles).foreach(i => panel.add(nextSlots(i), getConstraints(i)));

}
