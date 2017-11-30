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

import re.rizzi.subedit.substitles.{SubtitleHandler, SubtitleManager}

import scala.io.Source

class SubtitleHandlerTest extends  UnitTest {

  private val resourceUri = getClass.getResource("/sample.srt").toURI;

  private val firstSubtitle = "First sub";

  "A subtitle handler" should "handle a list of subtitles" in {
    val subtitleHandler = SubtitleHandler(resourceUri.getPath);
    subtitleHandler.isSuccess should be (true);
    SubtitleHandler(":invalidPath").isSuccess should be (false);
  }

  it should "provide access to the underlying subtitle manager" in {
    val subtitleHandler = SubtitleHandler(resourceUri.getPath);
    subtitleHandler.get.current should be (" ");
    subtitleHandler.get.currentStartTime should be (0);
    subtitleHandler.get.advance();
    subtitleHandler.get.current should be (firstSubtitle);
    subtitleHandler.get.currentStartTime should be (4000);
    subtitleHandler.get.back();
    subtitleHandler.get.current should be (" ");
    subtitleHandler.get.currentStartTime should be (0);
  }

  it should "provide access to the next subtitles" in {
    val subtitleHandler = SubtitleHandler(resourceUri.getPath);
    val nextTexts = subtitleHandler.get.next(5);
    subtitleHandler.get.advance();
    subtitleHandler.get.current should be(nextTexts.head);
    subtitleHandler.get.advance();
    subtitleHandler.get.current should be(nextTexts(1));
    subtitleHandler.get.advance();
    subtitleHandler.get.current should be(nextTexts(2));
  }

  it should "allow changing subtitle start and end time" in {
    val handler = SubtitleHandler(resourceUri.getPath).get;
    handler.advance();
    handler.setBeginTime(20);
    handler.setEndTime(45);
    val tempFile = File.createTempFile("output", "srt");
    tempFile.deleteOnExit();
    handler.save(tempFile.getPath);
    val strings = Source.fromFile(tempFile.getPath).getLines().toSeq;
    val manager = SubtitleManager(strings);
    manager(0).begin should be (20);
    manager(0).end should be (45);
    manager(0).text.mkString(" ") should be (firstSubtitle);
  }


}
