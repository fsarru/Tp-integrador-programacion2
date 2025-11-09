/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.List;

/**
 *
 * @author PC
 */
public interface GenericDAO<T> {
    // Esta es una interfaz genérica que define métodos comunes para trabajar con cualquier entidad.
//Agregado de SQLException y Connection conn: 
   void insertar(T entidad, Connection conn) throws SQLException;
    void actualizar(T entidad, Connection conn) throws SQLException;
    void eliminar(Long id, Connection conn) throws SQLException; 
    T getById(Long id, Connection conn) throws SQLException;
    List<T> getAll(Connection conn) throws SQLException;

}
