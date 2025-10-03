package com.gsm.service;

import com.gsm.model.Unit;
import com.gsm.repository.UnitRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The concrete implementation of the {@link UnitService} interface.
 */
@Service
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    public UnitServiceImpl(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void importFromExcel(InputStream inputStream) throws IOException {
        List<Unit> unitsToSave = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String unitCode = getCellValueAsString(row.getCell(0));
                if (unitCode.isEmpty()) continue;

                Optional<Unit> existingOpt = unitRepository.findByUnitCode(unitCode);
                Unit unit = existingOpt.orElseGet(() -> {
                    Unit newUnit = new Unit();
                    newUnit.setUnitCode(unitCode);
                    return newUnit;
                });

                unit.setUnitName(getCellValueAsString(row.getCell(1)));

                unitsToSave.add(unit);
            }
            if (!unitsToSave.isEmpty()) {
                unitRepository.saveAll(unitsToSave);
            }
        }
    }

    /**
     * Safely reads a cell's value and returns it as a String.
     * @param cell The Excel cell to read from.
     * @return The cell's content as a trimmed String.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }
}