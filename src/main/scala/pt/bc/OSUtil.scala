package pt.bc


object OSUtil extends Enumeration {
	type OS = Value
	val WindowsOS, MacOS, LinuxOS = Value

	private val map = Map(
		"windows" -> WindowsOS,
		"mac" -> MacOS,
		"linux" -> LinuxOS
	)

	def parse(s: String): OS = {
		map.map(e => if (s.toLowerCase().contains(e._1)) e).filter(_ != ()).headOption match {
			case Some(m) => m.asInstanceOf[(String, OS)]._2
			case _ => throw new Exception(s"Value $s cannot be parsed into an OSUtil")
		}
	}
}
