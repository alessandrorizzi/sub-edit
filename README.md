# Sub Edit
This is a very simple tool for synchronizing the subtitles
of a movie while watching it.

## Compile

Run `sbt assembly`.

## Launch

After compilation, 
run `java -jar target/scala-2.12/sub-edit.jar`.

## Usage

* After have opened a subtitle file (*.srt) and
the corresponding video, press `p` to start
playing the video.
Press `p` again to pause the video.
* Press `Space`to set the next subtitle.
The next five subtitle are shown at the bottom of the window.
* Press `Space` when the next subtitle starts and hold it
until the subtitle is active.
* Press `n` and `m` to move the video 10 seconds back and forward.
* Press `z` and `x` to skip to the previous/next subtitle without affecting the video.
* Press `q` and `w` to move the video to the previous/next subtitle.



