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
package re.rizzi.subedit.tests

import re.rizzi.subedit.substitles.Subtitle

import scala.io.Source

class SubtitleTest extends UnitTest {

  private val strings = Source.fromResource("sample.srt").getLines().span(_.nonEmpty)._1.toSeq;

  private val subtitle = Subtitle(strings).get;


  "A subtitle" should "contain a subtitle" in {
    subtitle.id should be (1);
    subtitle.begin should be (4000);
    subtitle.end should be (5555);
    Subtitle(Seq("foo", "bar", "foo")) should be (None);
    Subtitle(Seq("42")) should be (None);
    Subtitle(Seq("1", "2 3 4", "5")) should be (None);
  }

  it should "provide a textual representation" in {
    subtitle.toString should be (strings.mkString("\r\n") + "\r\n");
  }

  it should "be able to alter subtitles correctly" in {
    subtitle.withNewInterval(0, 7500).begin should be (0);
    subtitle.withNewInterval(0, 7500).end should be (7500);
    subtitle.withNewInterval(-10, 7500).begin should be (0);
    subtitle.withNewInterval(0, -2).end should be (0);
    subtitle.withNewInterval(10, 8).begin should be (10);
    subtitle.withNewInterval(10, 8).end should be (10);
    subtitle.withNewInterval(-2, -4).end should be (0);

  }

  it should "normalize the subtitle correctly avoiding overlap" in {
    val next = subtitle.withNewInterval(subtitle.end, subtitle.end + 2000);
    next.normalize(subtitle) should be (next);
    val overlap = subtitle.withNewInterval(subtitle.end - 1000, subtitle.end + 2000);
    overlap.normalize(subtitle) should be (next);
  }

}
