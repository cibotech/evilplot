package com.cibo.evilplot

import java.awt.{Color, EventQueue, Graphics, Graphics2D}

import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{FunctionPlot, Overlay, Plot, ScatterPlot}
import javax.swing.JPanel
import javax.swing.JFrame
import java.awt.event.{ActionEvent, KeyEvent}
import java.io.File

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

object displayPlot {

  object DrawablePanel extends JPanel {
    var drawable: Option[Drawable] = None

    def setDrawable(drawnPlot: Drawable): Unit = {
      drawable = Some(drawnPlot)
    }

    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      val g2 = g.asInstanceOf[Graphics2D]
      if (!(drawable == None)) {
        g2.drawImage(drawable.get.asBufferedImage, 0, 0, this)

      }
    }
  }

  object DrawableFrame extends JFrame {

    import javax.swing.JMenu
    import javax.swing.JMenuBar
    import javax.swing.JMenuItem

    var drawable: Option[Drawable] = None
    var plotTitle: String = "anonymous_plot"

    private def createMenuBar(): Unit = {
      val menubar = new JMenuBar
      val quit = new JMenuItem("Exit")
      quit.addActionListener((event: ActionEvent) => {
        def quit(event: ActionEvent) = {
          System.exit(0)
        }
        quit(event)
      })
      val save = new JMenuItem("Save")
      save.addActionListener((event: ActionEvent) => {
        def save(event: ActionEvent) = {
          drawable.get.write(new File(s"/tmp/$plotTitle.png"))
        }
        save(event)
      })
//      menubar.add(quit)
      menubar.add(save)
      setJMenuBar(menubar)
    }

    private def init(): Unit = {
      setTitle(plotTitle)
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      setSize(drawable.get.extent.width.toInt*4, drawable.get.extent.height.toInt*4 + 20)
      DrawablePanel.setDrawable(drawable.get)
      add(DrawablePanel)
      createMenuBar()
      setVisible(true)
    }

    def apply(drawnPlot: Drawable): Unit = {
      drawable = Some(drawnPlot)
      init()
    }

    def apply(drawnPlot: Drawable, withTitle: String): Unit = {
      drawable = Some(drawnPlot)
      plotTitle = withTitle
      init()
    }
  }


  def apply(drawnPlot: Drawable): Unit = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    DrawableFrame(drawnPlot)
  }

}

object testRunner extends App {
  val item =
    Overlay(
      ScatterPlot(Seq(Point(1,1), Point(2, 2), Point(3, 3))),
      FunctionPlot.series(i => i*i, "hey", HTMLNamedColors.dodgerBlue).standard()
    )
  displayPlot(item.render(Extent(400, 400)).scaled(0.5, 0.5))
}