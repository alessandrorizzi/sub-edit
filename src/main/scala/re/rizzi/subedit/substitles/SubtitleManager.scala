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

import java.io.{FileWriter, IOException}


sealed class SubtitleManager private (val subtitles : Array[Subtitle]) {

  def apply(index : Int) : Subtitle =
    if(index >= 0 && index < subtitles.length)
      subtitles(index);
    else
      Subtitle(-1, 0, 0, Seq(" "));

  def update(index : Int, begin : Long, end : Long) : Unit =
    if(index >= 0 && index < subtitles.length)
      subtitles(index) = subtitles(index).withNewInterval(begin, end);

  private def normalize() : Unit = {
    var i = 1;
    while(i < subtitles.length){
      subtitles(i) = subtitles(i).normalize(subtitles(i-1));
      i += 1;
    }
  }

  def write(path : String) : Boolean = {
    normalize();
    try {
      val writer = new FileWriter(path);
      try{
        writer.write(subtitles.mkString("\r\n"));
      }
      finally{
        writer.close();
      }
      true;
    }
    catch {
      case _ : IOException => false;
    }
  }

}


object SubtitleManager{

  private def split(input : Iterable[String]) : List[Seq[String]] = {
    val trimmed = input.dropWhile(_.isEmpty);
    if(trimmed.isEmpty) List();
    else {
      val (head, tail) = trimmed.span(_.nonEmpty);
      head.toSeq :: split(tail);
    }
  }

  def apply(lines : Iterable[String]) : SubtitleManager =
    new SubtitleManager(split(lines).flatMap(Subtitle(_)).toArray);

}
