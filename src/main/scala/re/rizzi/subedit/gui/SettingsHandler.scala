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

import java.awt.Color
import java.io.File
import javax.swing.UIManager
import javax.swing.UIManager.LookAndFeelInfo

import re.rizzi.subedit.utilities.{Localization, Settings}

class SettingsHandler(localization: Localization) extends Settings('settingsTitle, localization) {

  private val beginReactionTimeParameter = new LongParameter('begin, 300);

  private val endReactionTimeParameter = new LongParameter('end, 0);

  private val highlightedSubtitleColorParameter = new ColorParameter('highlightedSubtitleColor, Color.RED);

  private val subtitleColorParameter = new ColorParameter('subtitleColor, Color.BLUE);

  private val lastUsedFilePath = new StringParameter('lastUsedFilePath, "");

  private val lookAndFeelParameter =
    new ChoiceParameter[LookAndFeelInfo]('lookAndFeel, UIManager.getInstalledLookAndFeels, _.getName, "Nimbus");

  def lastUsedDirectory : Option[File] = {
    def existDirectory(path : String) = {
      lazy val file = new File(path);
      path.nonEmpty && file.exists() && file.isDirectory;
    }

    if(existDirectory(lastUsedFilePath.value))
      Some(new File(lastUsedFilePath.value));
    else
      None;
  }

  def updateLastUsedDirectory(path : String) : Unit = lastUsedFilePath.set(path);

  def beginReactionTime : Long = beginReactionTimeParameter.value;

  def endReactionTime : Long = endReactionTimeParameter.value;

  def highlightedSubtitleColor : Color = highlightedSubtitleColorParameter.value;

  def subtitleColor : Color = subtitleColorParameter.value;

  def lookAndFeel : LookAndFeelInfo = lookAndFeelParameter.value;

  override def parameters = Seq(beginReactionTimeParameter, endReactionTimeParameter,
    highlightedSubtitleColorParameter, subtitleColorParameter, lookAndFeelParameter);

}
