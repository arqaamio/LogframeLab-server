package com.arqaam.logframelab.util;

import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.util.Optional;

public class DocManipulationUtil {

    private DocManipulationUtil() {}

    /**
     * Insert table row with the same number of columns as the header of the table at the position
     * @param table Table to which the row will be added
     * @param pos   The index to which the row will be added
     */
    public static void insertTableRow(XWPFTable table, Integer pos) {
        XWPFTableRow row = table.insertNewTableRow(pos);
        int numCol = table.getRow(0).getTableCells().size();
        while(numCol != 0) {
            row.createCell();
            numCol--;
        }
    }

    /**
     * Merge cells of the same column
     * @param table    Table which has the cells
     * @param beginRow Index of the first row with the cells to be merged
     * @param endRow   Index of the last row with the cells to be merged
     * @param col      Index of the column from which the cells are from
     */
    public static void mergeCellsByColumn(XWPFTable table, int beginRow, int endRow, int col) {
        // First Row
        CTVMerge ctvMerge = CTVMerge.Factory.newInstance();
        ctvMerge.setVal(STMerge.RESTART);
        table.getRow(beginRow).getCell(col).getCTTc().getTcPr().setVMerge(ctvMerge);

        // Second Row
        CTVMerge ctvMerge1 = CTVMerge.Factory.newInstance();
        ctvMerge1.setVal(STMerge.CONTINUE);
        for (int i = beginRow+1; i <= endRow; i++) {
            table.getRow(i).getCell(col).getCTTc().addNewTcPr().setVMerge(ctvMerge1);
        }
    }

    /**
     * Merge cells of the same row
     * @param table    Table which has the cells
     * @param beginCol Index of the first row with the cells to be merged
     * @param endCol   Index of the last row with the cells to be merged
     * @param row      Index of the column from which the cells are from
     */
    public static void mergeCellsByRow(XWPFTable table, int beginCol, int endCol, int row) {
        // First Column
        CTHMerge cthMerge = CTHMerge.Factory.newInstance();
        cthMerge.setVal(STMerge.RESTART);
        if(table.getRow(row).getCell(beginCol).getCTTc().getTcPr() == null)
            table.getRow(row).getCell(beginCol).getCTTc().addNewTcPr().setHMerge(cthMerge);
        else
            table.getRow(row).getCell(beginCol).getCTTc().getTcPr().setHMerge(cthMerge);

        // Second Column
        CTHMerge cthMerge1 = CTHMerge.Factory.newInstance();
        cthMerge1.setVal(STMerge.CONTINUE);
        for (int i = beginCol+1; i <= endCol; i++) {
            table.getRow(row).getCell(i).getCTTc().addNewTcPr().setHMerge(cthMerge1);
        }
    }

    /**
     * Set text of the cell
     * @param cell     Cell to which the text is added
     * @param text     Text to be set
     * @param fontSize Optional: Font size of the text
     */
    public static void setTextOnCell(XWPFTableCell cell, String text, Integer fontSize) {
        while(cell.getParagraphs().size() !=0 ) {
            cell.removeParagraph(0);
        }
        XWPFRun run = cell.addParagraph().createRun();
        run.setText(text);
        // When removing the paragraphs, it removes the run and changes the font size
        Optional.ofNullable(fontSize).ifPresent(run::setFontSize);
    }

    /**
     * Creates a paragraph with a bold title and in a new line the assigned text
     * @param cell      Cell to which the text is added
     * @param titleText Text of the title to be set
     * @param text      Text to be set
     * @param fontSize  Optional: Font size of the text
     */
    public static void setTextOnCellWithBoldTitle(XWPFTableCell cell, String titleText, String text, Integer fontSize) {
        while(cell.getParagraphs().size() !=0 ) {
            cell.removeParagraph(0);
        }
        XWPFRun runTitle = cell.addParagraph().createRun();
        runTitle.setBold(true);
        runTitle.setText(titleText);
        if(text != null && !text.isEmpty()) {
            XWPFRun run = cell.addParagraph().createRun();
            run.setText(text);
            // When removing the paragraphs, it removes the run and changes the font size
            Optional.ofNullable(fontSize).ifPresent(run::setFontSize);
        }

        // When removing the paragraphs, it removes the run and changes the font size
        Optional.ofNullable(fontSize).ifPresent(runTitle::setFontSize);
    }

    /**
     * Set content of the cell as a hyperlink with the text
     * @param cell     Cell to where the hyperlink is set
     * @param text     Text to be set
     * @param uri      Link
     * @param fontSize Optional: Font size of the text
     */
    public static void setHyperLinkOnCell(XWPFTableCell cell, String text, String uri, Integer fontSize) {
        while(cell.getParagraphs().size() !=0 ) {
            cell.removeParagraph(0);
        }

        XWPFRun run = cell.addParagraph().createHyperlinkRun(uri);
        run.setText(text);
        // When removing the paragraphs, it removes the run and changes the font size
        Optional.ofNullable(fontSize).ifPresent(run::setFontSize);
    }
}
