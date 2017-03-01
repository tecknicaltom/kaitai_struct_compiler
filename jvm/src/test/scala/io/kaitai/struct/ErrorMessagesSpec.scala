package io.kaitai.struct

import java.io._
import java.nio.charset.Charset

import io.kaitai.struct.format.{KSVersion, YAMLParseException}
import io.kaitai.struct.formats.JavaKSYParser
import org.scalatest.FunSuite

class ErrorMessagesSpec extends FunSuite {
  // required, because this class is the sole entry point and this test needs
  // version info
  KSVersion.current = BuildInfo.version

  val FORMATS_ERR_DIR = "../tests/formats_err"
  val CHARSET_UTF8 = Charset.forName("UTF-8")

  def getExpected(fn: String): String = {
    val fis = new FileInputStream(fn)
    val isr = new InputStreamReader(fis, CHARSET_UTF8)
    val br = new BufferedReader(isr)
    val firstLine = br.readLine()

    if (firstLine.startsWith("# ")) {
      firstLine.substring(2)
    } else {
      "???"
      //fail(s"unable to parse expected line: $firstLine")
    }
  }

  def testOne(f: File): Unit = {
    val fileName = f.getName
    val testName = fileName.substring(0, fileName.length - 4)
    val fn = f.toString
    test(testName) {
      val expected = getExpected(fn)
      val caught = intercept[YAMLParseException] {
        val classSpec = JavaKSYParser.localFileToSpec(fn)
      }
      assertResult(expected) {
        // replace version-dependent message with a moniker
        caught.getMessage.replace(
          s"but you have ${KSVersion.current}",
          "but you have $KS_VERSION"
        )
      }
    }
  }

  new File(FORMATS_ERR_DIR).listFiles.
    filter((f) => f.isFile && f.getName.endsWith(".ksy")).
    sorted.foreach((f) => testOne(f))
}
