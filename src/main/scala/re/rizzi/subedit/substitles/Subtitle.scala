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

import scala.util.Try

sealed case class Subtitle(id : Int, begin : Long, end : Long, text : Seq[String]) {

  private def interval : String = Subtitle.fromLong(begin) + " --> " + Subtitle.fromLong(end) + "\r\n";

  override def toString : String = id + "\r\n" + interval + text.mkString("\r\n") + "\r\n";

  def withNewInterval(b : Long, e : Long) : Subtitle = Subtitle(id, Math.max(0, b), Math.max(Math.max(0, b), e), text);

  def normalize(previous : Subtitle) : Subtitle =
    if(begin > previous.begin && begin < previous.end && end > previous.end)
      Subtitle(id, previous.end, end, text);
    else
      this;

}

object Subtitle {

  private def fromLong(input : Long) : String = {
    val milliseconds = input % 1000;
    val seconds = (input/1000)%60;
    val minutes = (input/60000)%60;
    val hours = input/3600000;
    "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, milliseconds);
  }

  private def toLong(input : String) : Option[Long] = {
    val parts = input.split(":").toList;
    parts match {
      case h :: m :: s :: Nil => Some(h.toLong* 3600000 + m.toLong*60000 + s.replaceAll(",","").toLong);
      case _ => None;
    }
  }

  private def toTime(input : String) : Option[(Long, Long)] = {
    val split = input.split(" ");
    toLong(split.head).flatMap(x => toLong(split.last).map(y => (x,y)));
  }

  def apply(input : Seq[String]) : Option[Subtitle] = {
    lazy val id = Try(input.head.replace('﻿', ' ').trim.toInt);
    if(input.size < 2 || id.isFailure){
      None;
    }
    else{
      val time = toTime(input(1));
      val text = input.drop(2);
      time.map {
        t =>
          val (begin, end) = t;
          Subtitle(id.get, begin, end, text);
      }
    }
  }


}