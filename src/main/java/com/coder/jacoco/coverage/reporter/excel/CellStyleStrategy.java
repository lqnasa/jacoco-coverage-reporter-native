package com.coder.jacoco.coverage.reporter.excel;

import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

public class CellStyleStrategy extends AbstractCellStyleStrategy {

    @Override
    public int order() {
        return 10000000;
    }

    @Override
    protected void setHeadCellStyle(CellWriteHandlerContext context) {
        Workbook workbook = context.getWriteWorkbookHolder().getWorkbook();
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置头字体
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 13);
        headWriteFont.setBold(true);
        headWriteCellStyle.setWriteFont(headWriteFont);
        //设置头居中
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        Cell cell = context.getCell();
        CellStyle cellStyle = StyleUtil.buildCellStyle(workbook, cell.getCellStyle(), headWriteCellStyle);
        cell.setCellStyle(cellStyle);
    }


    @Override
    protected void setContentCellStyle(CellWriteHandlerContext context) {
        //内容策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        //垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Workbook workbook = context.getWriteWorkbookHolder().getWorkbook();
        Cell cell = context.getCell();
        CellStyle cellStyle = StyleUtil.buildCellStyle(workbook, cell.getCellStyle(), contentWriteCellStyle);
        cell.setCellStyle(cellStyle);
    }


}