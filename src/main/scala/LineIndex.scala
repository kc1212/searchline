import java.nio.file.Path

import scala.util.Try

object LineIndex {
  def newIndex(iter: DoubleIterator[Byte]): List[Int] = {
    if (iter.isEmpty) List()
    else {
      0 :: iter.zip { Iterator.from(0) }
        .foldLeft(List[Int]()) { (idx, b) =>
          if (b._1 == '\n') b._2 + 1 :: idx
          else idx
        }
        .reverse
    }
  }

  def saveIndex(idx: List[Int]): Try[Nothing] = ???
  def loadIndex(f: Path): Try[List[Int]] = ???
}
