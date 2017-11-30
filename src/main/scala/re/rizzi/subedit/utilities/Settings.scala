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
package re.rizzi.subedit.utilities

import java.awt.event.{ActionEvent, ActionListener, WindowEvent}
import java.awt._
import java.util.prefs.Preferences
import javax.swing._

abstract class Settings(title : Symbol, localization: Localization) {

  private val preferences = Preferences.userNodeForPackage(this.getClass);

  abstract class Parameter(val description : Symbol){

    def controller : JComponent;

    def accept(dialog: JDialog) : Boolean;

    protected def getInt(default : Int) : Int = preferences.getInt(description.name, default);

    protected def getLong(default : Long) : Long = preferences.getLong(description.name, default);

    protected def getString(default : String) : String = preferences.get(description.name, default);

    protected def setInt(value : Int) : Unit = preferences.putInt(description.name, value);

    protected def setLong(value : Long) : Unit = preferences.putLong(description.name, value);

    protected def setString(value : String) : Unit = preferences.put(description.name, value);

  }

  class ChoiceParameter[E](description : Symbol, choices : Seq[E], identifiers : E => String, default : String)
    extends  Parameter(description){

    assert(choices.nonEmpty);

    private val choiceMap = choices.map(choice => identifiers(choice) -> choice).toMap;

    private def defaultValue = choiceMap.getOrElse(default, choices.head);

    private var actualValue : String = getString(default);

    def value : E = choiceMap.getOrElse(actualValue, defaultValue);

    def set(value : String) : Unit = {
      setString(value);
      actualValue = getString(default);
      comboBox.setSelectedItem(actualValue);
    }

    private val comboBox = new JComboBox[String](choiceMap.keys.toArray);
    comboBox.setSelectedItem(actualValue);
    comboBox.setPreferredSize(new Dimension(Math.max(100, comboBox.getPreferredSize.width), comboBox.getPreferredSize.height));

    override def controller: JComponent = comboBox;

    override def accept(dialog: JDialog): Boolean = {
      set(comboBox.getSelectedItem.asInstanceOf[String]);
      true;
    }
  }

  class StringParameter(description : Symbol, default : String) extends Parameter(description) {

    private var actualValue : String = getString(default);

    private val edit = new JTextField(actualValue.toString);

    edit.setPreferredSize(new Dimension(Math.max(100, edit.getPreferredSize.width), edit.getPreferredSize.height));

    override def controller: JComponent = edit;

    override def accept(dialog: JDialog): Boolean =
      try{
        set(edit.getText);
        true;
      }catch {
        case _  : NumberFormatException =>
          JOptionPane.showMessageDialog(dialog, localization.tr('invalidLong));
          false;
      }

    def value : String = actualValue;

    def set(value : String) : Unit = {
      setString(value);
      actualValue = getString(default);
    }

  }

  class ColorParameter(description : Symbol, default : Color) extends Parameter(description) {

    private var actualValue : Color = new Color(getInt(default.getRGB));

    private val button = new JButton();
    button.setBackground(actualValue);
    button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    button.addActionListener(PickColor);

    object PickColor extends ActionListener {
      override def actionPerformed(actionEvent: ActionEvent): Unit = {
        val newColor = JColorChooser.showDialog(button.getTopLevelAncestor, "Choose a color", button.getBackground);
        button.setBackground(newColor);
      }
    }

    override def controller: JComponent = button;

    override def accept(dialog: JDialog): Boolean = {
      setInt(button.getBackground.getRGB);
      actualValue = new Color(getInt(default.getRGB));
      true;
    }

    def value : Color = actualValue;

  }

  class LongParameter(description : Symbol, default : Long) extends Parameter(description) {

    private var actualValue : Long = getLong(default);

    private val edit = new JTextField(actualValue.toString);

    edit.setPreferredSize(new Dimension(Math.max(100, edit.getPreferredSize.width), edit.getPreferredSize.height));

    override def controller: JComponent = edit;

    override def accept(dialog: JDialog): Boolean =
      try{
        setLong(edit.getText.toLong);
        actualValue = getLong(default);
        true;
      }catch {
        case _  : NumberFormatException =>
          JOptionPane.showMessageDialog(dialog, localization.tr('invalidLong));
          false;
      }

    def value : Long = actualValue;

  }

  def parameters : Seq[Parameter];

  private class DialogListener(dialog : JDialog, ok : Boolean) extends ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      if(!ok || parameters.forall(_.accept(dialog)))
        dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
    }
  }


  def show(frame: Frame) : JDialog = {
    val result = new JDialog(frame);
    result.setLayout(new GridBagLayout());
    parameters.zipWithIndex.foreach{p =>
      val label = new JLabel(localization.tr(p._1.description));
      val constraint = new GridBagConstraints();
      constraint.gridx = 0;
      constraint.weightx = 0.1;
      constraint.gridy = p._2;
      constraint.fill = GridBagConstraints.BOTH;
      constraint.insets = new Insets(20, 20, 0, 20);
      result.add(label, constraint);
      constraint.gridx = 1;
      constraint.weightx = 1;
      result.add(p._1.controller, constraint);
    }
    val buttons = new JPanel();
    val buttonsLayout = new GridBagLayout();
    buttons.setLayout(buttonsLayout);
    val ok = new JButton(localization.tr('confirm));
    val cancel = new JButton(localization.tr('cancel));
    val constraint = new GridBagConstraints();
    constraint.anchor = GridBagConstraints.CENTER;
    constraint.gridx = 0;
    constraint.weightx = 1;
    constraint.gridy = 0;//parameters.size;
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.insets = new Insets(20, 20, 20, 20);
    ok.addActionListener(new DialogListener(result, true));
    cancel.addActionListener(new DialogListener(result, false));
    buttons.add(ok, constraint);
    constraint.gridx = 1;
    buttons.add(cancel, constraint);

    val buttonsConstraint = new GridBagConstraints();
    buttonsConstraint.gridx = 0;
    buttonsConstraint.gridy = parameters.size;
    buttonsConstraint.gridwidth = 2;
    buttonsConstraint.fill = GridBagConstraints.HORIZONTAL;
    result.add(buttons, buttonsConstraint);

    result.setTitle(localization.tr(title));
    result.pack();
    result.setVisible(true);
    result;
  }
}
