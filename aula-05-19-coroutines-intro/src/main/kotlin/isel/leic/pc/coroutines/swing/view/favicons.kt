package isel.leic.pc.coroutines.swing.view


import isel.leic.pc.coroutines.swing.model.GetIconParams
import isel.leic.pc.coroutines.swing.model.processShowIconsParallel
import isel.leic.pc.coroutines.swing.model.processShowIconsSerial
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import mu.KotlinLogging
import pc.li52d.web.getIcon
import java.awt.BorderLayout

import java.awt.BorderLayout.*
import java.awt.GridLayout
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.*
import javax.swing.WindowConstants.EXIT_ON_CLOSE


// Place definition above class declaration to make field static
val logger = KotlinLogging.logger {}

class FavIconsApp {


    private val SwingScope = CoroutineScope(Dispatchers.Swing)

    private val frame = JFrame()
    private val pic1Label = JLabel()
    private val pic2Label = JLabel()
    private val pic3Label = JLabel()
    private val url1Text = JTextField()
    private val url2Text = JTextField()
    private val url3Text = JTextField()
    private val testButton = JButton("+")
    private val clicksView = JTextField(16)
    private val showBut = JButton()

    private fun initComponents() {
        url1Text.columns = 32
        url1Text.text = "https://tvi.iol.pt/favicon.ico"
        url2Text.columns = 32
        url2Text.text = "https://www.tsf.pt/favicon.ico"
        url3Text.columns = 32
        url3Text.text ="https://www.rtp.pt/favicon.ico"

        showBut.text = "Show Favourites"
        val pane = frame.contentPane

        val inputPane = JPanel(BorderLayout())
        inputPane.add(showBut, SOUTH)

        val urlPane = JPanel(GridLayout(3, 1))
        urlPane.add(url1Text)
        urlPane.add(url2Text)
        urlPane.add(url3Text)
        inputPane.add(urlPane, CENTER)

        pane.add(inputPane, NORTH)

        pic1Label.text = "Pic 1"
        pic2Label.text = "Pic 2"
        pic3Label.text = "Pic 3"
        val picturesPanel = JPanel(GridLayout(1, 3))
        picturesPanel.add(pic1Label)
        picturesPanel.add(pic2Label)
        picturesPanel.add(pic3Label)
        //pane.add(htmlTextArea)
        pane.add(picturesPanel, CENTER)

        val testPanel = JPanel()
        clicksView.text = "0"
        testPanel.add(testButton)
        testPanel.add(clicksView)

        pane.add(testPanel, SOUTH)
    }

    private fun processIcon(img : BufferedImage ) : ImageIcon {
        val butImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH)
        return ImageIcon(butImg)
    }



    private fun initEvents() {
        showBut.addActionListener {
            logger.info("start showBut  handler")

            SwingScope.launch {
                logger.info("start processShowIcons")
                processShowIconsParallel(
                    GetIconParams(url1Text.text, 2000, pic1Label),
                    GetIconParams(url2Text.text, 2000, pic2Label),
                    GetIconParams(url3Text.text, 2000, pic3Label),

                )
            }
            logger.info("end showBut handler in thread")
        }

        testButton.addActionListener {
            logger.info("testButton Handler" )
            val num = Integer.parseInt(clicksView.text) + 1
            clicksView.text = num.toString()
        }
    }

    init {
        initComponents()
        initEvents()
        frame.setSize(500,300)
        frame.defaultCloseOperation = EXIT_ON_CLOSE
        frame.isVisible = true
    }
}

private fun main() {
    val app = FavIconsApp()
}