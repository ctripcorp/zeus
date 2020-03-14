package com.ctrip.zeus.flow.vsmigration;

import com.ctrip.zeus.model.tools.VsMigration;

import java.util.List;

public interface FlowVsMigrationService {
    // New Vs Migration Method
    VsMigration newVsMigration(VsMigration migration) throws Exception;

    // Update Vs Migration Method
    VsMigration updateVsMigration(VsMigration migration) throws Exception;

    // Get Vs Migration Method
    VsMigration getVsMigration(long id) throws Exception;

    // Get Vs Migration by status Method
    List<VsMigration> getAllMigrationByStatus(boolean activated) throws Exception;

    // Get All Vs Migrations
    List<VsMigration> getAllMigration() throws Exception;

    // Delete Vs migration
    boolean deleteVsMigration(VsMigration migration) throws Exception;

    // Clean Vs migration from db
    boolean clearVsMigration(VsMigration migration) throws Exception;
}
