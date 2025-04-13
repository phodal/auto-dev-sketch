package cc.unitmesh.devti.mcp.ui

import cc.unitmesh.devti.mcp.ui.model.McpMessage
import cc.unitmesh.devti.mcp.ui.model.MessageType
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class McpMessageLogPanel : JPanel(BorderLayout()) {
    private val messages = mutableListOf<McpMessage>()
    private val tableModel = MessageTableModel()
    private val table = JBTable(tableModel).apply {
        setShowGrid(false)
        intercellSpacing = Dimension(0, 0)
        rowHeight = 30
        selectionModel = DefaultListSelectionModel().apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
        }
        autoCreateRowSorter = true
    }

    private val detailTextArea = JTextArea().apply {
        isEditable = false
        wrapStyleWord = true
        lineWrap = true
        border = JBUI.Borders.empty(10)
    }

    init {
        table.getColumnModel().getColumn(0).cellRenderer = TypeColumnRenderer()
        
        // Create split pane
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
            leftComponent = JBScrollPane(table)
            rightComponent = JBScrollPane(detailTextArea)
            dividerLocation = 600
            resizeWeight = 0.5
        }
        
        add(splitPane, BorderLayout.CENTER)
        table.selectionModel.addListSelectionListener { e ->
            if (!e.valueIsAdjusting && table.selectedRow >= 0) {
                val selectedIndex = table.convertRowIndexToModel(table.selectedRow)
                if (selectedIndex >= 0 && selectedIndex < messages.size) {
                    detailTextArea.text = messages[selectedIndex].content
                    detailTextArea.caretPosition = 0
                }
            }
        }
    }
    
    fun addMessage(message: McpMessage) {
        messages.add(message)
        tableModel.fireTableDataChanged()
        SwingUtilities.invokeLater {
            table.setRowSelectionInterval(messages.size - 1, messages.size - 1)
        }
    }
    
    fun clear() {
        messages.clear()
        tableModel.fireTableDataChanged()
        detailTextArea.text = ""
    }
    
    private inner class MessageTableModel : DefaultTableModel() {
        private val columnNames = arrayOf(
            "Type",
            "Method",
            "Timestamp",
            "Duration"
        )
        
        override fun getColumnCount(): Int = columnNames.size
        
        override fun getRowCount(): Int = messages.size
        
        override fun getColumnName(column: Int): String = columnNames[column]
        
        override fun getValueAt(row: Int, column: Int): Any {
            val message = messages[row]
            return when (column) {
                0 -> message.type
                1 -> message.method
                2 -> message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                3 -> message.duration?.toString() ?: "-"
                else -> ""
            }
        }
        
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }
    
    private class TypeColumnRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel
            label.horizontalAlignment = SwingConstants.CENTER
            
            if (!isSelected) {
                when (value) {
                    MessageType.REQUEST -> {
                        label.background = JBColor(0xE1F5FE, 0x0D47A1) // Light blue for request
                        label.foreground = JBColor(0x01579B, 0xE3F2FD)
                    }
                    MessageType.RESPONSE -> {
                        label.background = JBColor(0xE8F5E9, 0x2E7D32) // Light green for response
                        label.foreground = JBColor(0x1B5E20, 0xE8F5E9)
                    }
                }
            }
            
            label.text = when (value) {
                MessageType.REQUEST -> "REQUEST"
                MessageType.RESPONSE -> "RESPONSE"
                else -> ""
            }
            
            label.border = JBUI.Borders.empty(3, 5)
            
            return label
        }
    }
}
