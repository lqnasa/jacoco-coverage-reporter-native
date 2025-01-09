package com.coder.jacoco.coverage.reporter.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelFillCellMergePrevColStrategy implements CellWriteHandler {
    private static final String KEY = "%s-%s";
    /**
     * 所有的合并信息都存在了这个map里面
     */
    Map<String, Integer> mergeInfo = new HashMap<>();

    public ExcelFillCellMergePrevColStrategy() {
    }

    public ExcelFillCellMergePrevColStrategy(Map<String, Integer> mergeInfo) {
        this.mergeInfo = mergeInfo;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList,
                                 Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //当前行
        int curRowIndex = cell.getRowIndex();
        //当前列
        int curColIndex = cell.getColumnIndex();

        Integer num = mergeInfo.get(String.format(KEY, curRowIndex, curColIndex));
        if (null != num) {
            // 合并最后一行 ,列
            mergeWithPrevCol(writeSheetHolder, cell, curRowIndex, curColIndex, num);
        }
    }

    public void mergeWithPrevCol(WriteSheetHolder writeSheetHolder, Cell cell, int curRowIndex, int curColIndex, int num) {
        Sheet sheet = writeSheetHolder.getSheet();
        CellRangeAddress cellRangeAddress = new CellRangeAddress(curRowIndex, curRowIndex, curColIndex, curColIndex + num);
        sheet.addMergedRegion(cellRangeAddress);
    }

    /**
     * um从第几列开始增加多少列
     * 比如我上图中中心需要在第三行 从0列开始合并三列 所以我可以传入 (3,0,2)
     *
     * @param curRowIndex 在第几行进行行合并
     * @param curColIndex 在第几列进行合并
     * @param num         合并多少格
     */
    public void add(int curRowIndex, int curColIndex, int num) {
        mergeInfo.put(String.format(KEY, curRowIndex, curColIndex), num);
    }

}