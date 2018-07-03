package com.cibo.evilplot

import java.awt.{Graphics, Graphics2D}

import com.cibo.evilplot.plot.{Plot}
import javax.swing.{JFrame, JOptionPane, JPanel}
import java.awt.event.{ActionEvent, ComponentAdapter, ComponentEvent}
import java.io.File

import com.cibo.evilplot.demo.DemoPlots.{theme}
import com.cibo.evilplot.geometry.{Drawable, Extent}

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
        g2.drawImage(drawable.get.asBufferedImage, -40, 0, this)

      }
    }
  }

  object DrawableFrame extends JFrame {

    import javax.swing.JMenuBar
    import javax.swing.JMenuItem

    var drawable: Option[Drawable] = None
    var plot: Option[Plot] = None


    private def createMenuBar(): Unit = {
      val menubar = new JMenuBar
      val save = new JMenuItem("Save")
      save.addActionListener((event: ActionEvent) => {
        def save(event: ActionEvent) = {
          val result = JOptionPane.showInputDialog("Enter a filename:")
          savePlot(result)
        }
        save(event)
      })
      menubar.add(save)
      setJMenuBar(menubar)
    }

    private def init(): Unit = {
      setTitle("Plot")
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      if (!drawable.isEmpty) {
        setSize(drawable.get.extent.width.toInt*2, drawable.get.extent.height.toInt*2 + 20)
        DrawablePanel.setDrawable(drawable.get.scaled(0.5,0.5))
      }
      else {
        setSize(400, 420)
        DrawablePanel.setDrawable(plot.get.render(new Extent(200, 200)).scaled(0.5, 0.5))
      }
      add(DrawablePanel)
      createMenuBar()
      addComponentListener(new ComponentAdapter {
        override def componentResized(e: ComponentEvent): Unit = {
          resizePlot(DrawableFrame.getWidth, DrawableFrame.getHeight)
        }
      })
      setVisible(true)
    }

    def getPlotExtent: Extent = {
      Extent(this.getWidth/2, (this.getHeight-20)/2)
    }

    def resizePlot(width: Int, height: Int): Unit = {
      if (!plot.isEmpty) {
        DrawablePanel.setDrawable(plot.get.render(getPlotExtent).scaled(0.5,0.5))
      }
    }

    def savePlot(result: String): Unit = {
      if(!plot.isEmpty) {
        plot.get.render(getPlotExtent).scaled(0.5,0.5).write(new File(s"/tmp/$result.png"))
      } else {
        drawable.get.write(new File(s"/tmp/$result.png"))
      }
    }

    def apply(drawnPlot: Option[Drawable], notDrawnPlot: Option[Plot]): Unit = {
      drawable = drawnPlot
      plot = notDrawnPlot
      init()
    }

  }

  def apply(plot: Plot): Unit = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    DrawableFrame(None, Some(plot))
  }

  def apply(drawnPlot: Drawable): Unit = {
    JFrame.setDefaultLookAndFeelDecorated(true)
    DrawableFrame(Some(drawnPlot), None)
  }

}

