package com.arqaam.logframelab.util;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

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
        if(table.getRow(beginRow).getCell(col).getCTTc().getTcPr() == null)
            table.getRow(beginRow).getCell(col).getCTTc().addNewTcPr().setVMerge(ctvMerge);
        else
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
    /**
     * copy a paragraph in document
     * @param source    the paragraph to be copied
     * @param target    the paragraph to copy to
     */
    private static void copyParagraph(XWPFParagraph source, XWPFParagraph target) {
        target.getCTP().setPPr(source.getCTP().getPPr());
        for (int i=0; i<source.getRuns().size(); i++ ) {
            XWPFRun run = source.getRuns().get(i);
            XWPFRun targetRun = target.createRun();
            //copy formatting
            targetRun.getCTR().setRPr(run.getCTR().getRPr());
            //no images just copy text
            targetRun.setText(run.getText(0));
        }
    }
    /**
     * copy a table in document
     * @param source    the table to be copied
     * @param target    the table to copy to
     */
    public static void copyTable(XWPFTable source, XWPFTable target) {
        target.getCTTbl().setTblPr(source.getCTTbl().getTblPr());
        target.getCTTbl().setTblGrid(source.getCTTbl().getTblGrid());
        for (int r = 0; r<source.getRows().size(); r++) {
            XWPFTableRow targetRow = target.createRow();
            XWPFTableRow row = source.getRows().get(r);
            targetRow.getCtRow().setTrPr(row.getCtRow().getTrPr());
            for (int c=0; c<row.getTableCells().size(); c++) {
                //newly created row has 1 cell
                XWPFTableCell targetCell = c==0 ? targetRow.getTableCells().get(0) : targetRow.createCell();
                XWPFTableCell cell = row.getTableCells().get(c);
                targetCell.getCTTc().setTcPr(cell.getCTTc().getTcPr());
                XmlCursor cursor = targetCell.getParagraphArray(0).getCTP().newCursor();
                for (int p = 0; p < cell.getBodyElements().size(); p++) {
                    IBodyElement elem = cell.getBodyElements().get(p);
                    if (elem instanceof XWPFParagraph) {
                        XWPFParagraph targetPar = targetCell.insertNewParagraph(cursor);
                        cursor.toNextToken();
                        XWPFParagraph par = (XWPFParagraph) elem;
                        copyParagraph(par, targetPar);
                    } else if (elem instanceof XWPFTable) {
                        XWPFTable targetTable = targetCell.insertNewTbl(cursor);
                        XWPFTable table = (XWPFTable) elem;
                        copyTable(table, targetTable);
                        cursor.toNextToken();
                    }
                }
                //newly created cell has one default paragraph we need to remove
                targetCell.removeParagraph(targetCell.getParagraphs().size()-1);
            }
        }
        //newly created table has one row by default. we need to remove the default row.
        target.removeRow(0);
    }

    public static void setTextWithBreakOnCell(XWPFTable activityTable,int rowIndex, String text, Integer fontSize ,int cellIndex ,boolean setBold ,boolean addBreak) {
        XWPFRun run = activityTable.getRow(rowIndex).getCell(cellIndex).addParagraph().createRun();
        if(setBold){
            run.setBold(true);
        }
        run.setText(text);
        if(addBreak){
            run.addBreak();
        }
        Optional.of(fontSize).ifPresent(run::setFontSize);
    }

}
