package com.cibo.evilplot

object DOMInitializer {
  import org.scalajs.dom

  def init(): Unit = {
    val node = dom.document.createElement(Utils.canvas)
    node.setAttribute("id", Utils.measureBuffer)
    dom.document.body.appendChild(node)
  }
}
