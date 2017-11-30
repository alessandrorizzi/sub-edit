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

import java.io.File

import re.rizzi.subedit.substitles.SubtitleManager

import scala.io.Source

class SubtitleManagerTest extends  UnitTest {

  private val strings = Source.fromResource("sample.srt").getLines().toSeq;

  private val subtitleManager = SubtitleManager(strings);


  "A subtitle manager" should "contain a list of subtitles" in {
    subtitleManager.apply(9).begin should be (610100);
    subtitleManager.apply(10).end should be (0);
    subtitleManager.apply(-1).begin should be (0);
    subtitleManager.apply(65536).end should be (0);
  }

  it should "perform write correctly" in {
    val tempFile = File.createTempFile("output", "srt");
    tempFile.deleteOnExit();
    subtitleManager.write(tempFile.getPath) should be (true);
    Source.fromFile(tempFile).getLines().toSeq should be (strings);
  }

  it should "handle the case of an impossible write" in {
    subtitleManager.write("http://somedomain.com") should be (false);
    val tempFile = File.createTempFile("output", "srt");
    tempFile.deleteOnExit();
    tempFile.setWritable(false);
    subtitleManager.write(tempFile.getPath) should be (false);
  }

  it should "provide update operation for single subtitle" in {
    subtitleManager.update(-1, 0, 277);
    subtitleManager.update(65536, 0, 299);
    subtitleManager.update(0, 0, 42);
    subtitleManager(0).end should be (42);
  }


}
