package net.citizensnpcs.api.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler {
   Object handle(ResultSet var1) throws SQLException;
}
