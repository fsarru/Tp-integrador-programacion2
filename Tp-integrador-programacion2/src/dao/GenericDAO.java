package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface GenericDAO<T> {
    
    // Todos los métodos aceptan Connection conn y propagan SQLException
    void insertar(T entidad, Connection conn) throws SQLException;
    void actualizar(T entidad, Connection conn) throws SQLException;
    void eliminar(Long id, Connection conn) throws SQLException; // ID LONG para BIGINT y Baja Lógica
    T getById(Long id, Connection conn) throws SQLException;
    List<T> getAll(Connection conn) throws SQLException;
    
}
