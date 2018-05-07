package pt.bc.test

import pt.bc.OSUtil

object OSTest {
	def main(args: Array[String]): Unit = {
		val os = System.getProperty("os.name")
		val version = System.getProperty("os.version")
		val arch = System.getProperty("os.arch")

		println(os, version, arch)

		val osparsed = OSUtil.parse(os)
		println(osparsed)
	}
}
