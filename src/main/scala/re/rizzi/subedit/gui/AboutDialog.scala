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

import java.awt.{GridBagConstraints, GridBagLayout}

import javax.swing._
import re.rizzi.subedit.utilities.Localization

class AboutDialog(localization: Localization, parent : JFrame) extends JDialog {

  private val icon : Icon = new ImageIcon(getClass.getResource("/logo.png"));

  private def getConstraints(i: Int) : GridBagConstraints = {
    val result = new GridBagConstraints();
    result.fill = GridBagConstraints.HORIZONTAL;
    result.fill = GridBagConstraints.VERTICAL;
    result.ipadx = 20;
    result.ipady = if(i==0) 85; else 10;
    result.gridx = 0;
    result.gridy = i;
    result;
  }

  setLayout(new GridBagLayout());

  add(new JLabel(icon), getConstraints(0));
  add(new JLabel("<html><b>" + localization.tr('appTitle) + "</b></html>"), getConstraints(1));
  add(new JLabel(localization.tr('version)), getConstraints(2));
  add(new JLabel(localization.tr('description)), getConstraints(3));
  add(new JLabel(localization.tr('copyrightInfo)), getConstraints(4));

  setTitle(localization.tr('aboutDialogTitle));
  setSize(400, 350);
  setLocationRelativeTo(parent);
  setVisible(true);

}
