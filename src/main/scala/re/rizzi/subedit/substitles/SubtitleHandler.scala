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

import java.nio.charset.MalformedInputException

import scala.io.Source
import scala.util.Try

sealed class SubtitleHandler protected (manager : SubtitleManager) {

  private var index = -1;

  def setBeginTime(time: Long): Unit = manager.update(index, time, manager(index).end);

  def setEndTime(time: Long): Unit = manager.update(index, manager(index).begin, time);

  def save(filename : String) : Boolean = manager.write(filename);

  def next(n : Int) : Seq[String] =
    Array.range(0, n).map(i => manager(index + 1 + i)).map(_.text.mkString(" "));

  def current : String = manager(index).text.mkString(" ");

  def currentStartTime: Long = manager(index).begin;

  def advance() : Unit = index += 1;

  def back() : Unit = index -= 1;

}

object NullSubtitleHandler extends SubtitleHandler(SubtitleManager(Seq()));

object SubtitleHandler {


  private def tryGetLines(path : String, encoding : String) : Option[Seq[String]] = {
    try{
      Some(Source.fromFile(path, encoding).getLines().toArray.toSeq);
    }
    catch {
      case _ : MalformedInputException => None;
    }
  }

  private def getLines(path : String) = {
    tryGetLines(path, "UTF-8").orElse(tryGetLines(path, "ISO-8859-1")).getOrElse(Seq());
  }

  def apply(path : String) : Try[SubtitleHandler] = Try{
    val lines = getLines(path);
    val subtitleManager = SubtitleManager(lines)
    new SubtitleHandler(subtitleManager);
  }

}