package services;

import config.DatabaseConnection;
import dao.HistoriaClinicaDAO;
import dao.PacienteDAO;
import entities.HistoriaClinica;
import entities.Paciente;
import entities.GrupoSanguineo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class PacienteService {

    private final PacienteDAO pacienteDAO;
    private final HistoriaClinicaDAO historiaDAO;

    public PacienteService() {
        this.historiaDAO = new HistoriaClinicaDAO();
        this.pacienteDAO = new PacienteDAO(historiaDAO);
    }

    // =====================================================
    // CREAR PACIENTE + ASIGNAR HC AUTOMÁTICA COMPLETA
    // =====================================================
    public void crearPacienteAsignandoHistoria(Paciente p,
                                               String antecedentes,
                                               String medicacion,
                                               String grupoSanguineoStr,
                                               String observaciones) throws Exception {

        try (Connection conn = DatabaseConnection.getConnection()) {

            try {
                conn.setAutoCommit(false);

                // 1) Buscar HC libre o crear una nueva
                HistoriaClinica h = obtenerHistoriaClinicaAutomatica(conn);

                // 2) Actualizar con datos ingresados por usuario
                h.setGrupoSanguineo(GrupoSanguineo.fromValor(grupoSanguineoStr));
                h.setAntecedentes(antecedentes);
                h.setMedicacionActual(medicacion);
                h.setObservaciones(observaciones);

                historiaDAO.actualizar(h, conn);

                // 3) Asociar HC al paciente
                p.setHistoriaClinica(h);

                // 4) Insertar paciente
                pacienteDAO.insertar(p, conn);

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // =====================================================
    // OBTENER HC LIBRE O CREAR NUEVA
    // =====================================================
    private HistoriaClinica obtenerHistoriaClinicaAutomatica(Connection conn) throws SQLException {

        String sql =
            "SELECT h.* FROM historiaclinica h " +
            "LEFT JOIN paciente p ON h.id = p.historiaClinica " +
            "WHERE p.idPaciente IS NULL " +
            "ORDER BY h.id LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new HistoriaClinica(
                        rs.getLong("id"),
                        rs.getLong("nroHistoria"),
                        GrupoSanguineo.fromValor(rs.getString("grupoSanguineo")),
                        rs.getString("antecedentes"),
                        rs.getString("medicacionActual"),
                        rs.getString("observaciones")
                );
            }
        }

        // Si no hay HC disponible → crear nueva
        return crearNuevaHistoriaClinica(conn);
    }

    // =====================================================
    // CREAR NUEVA HISTORIA CLÍNICA
    // =====================================================
    private HistoriaClinica crearNuevaHistoriaClinica(Connection conn) throws SQLException {

        HistoriaClinica nueva = new HistoriaClinica(
                0L,
                0L,
                GrupoSanguineo.fromValor("O+"),
                "",
                "",
                "Historia generada automáticamente"
        );

        historiaDAO.insertar(nueva, conn);

        // Recargar nroHistoria generado
        String sql = "SELECT nroHistoria FROM historiaclinica WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, nueva.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nueva.setNroHistoria(rs.getLong("nroHistoria"));
            }
        }

        return nueva;
    }

    // =====================================================
    // CRUD COMPLETO
    // =====================================================

    public List<Paciente> obtenerTodos() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return pacienteDAO.getAll(conn);
        }
    }

    public Paciente buscarPorDni(String dniStr) throws Exception {
        long dni = Long.parseLong(dniStr);
        try (Connection conn = DatabaseConnection.getConnection()) {
            return pacienteDAO.buscarPorCampoUnicoLong(dni, conn);
        }
    }

    public Paciente obtenerPorId(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return pacienteDAO.getById(id, conn);
        }
    }

    public void actualizar(Paciente p) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            pacienteDAO.actualizar(p, conn);
        }
    }

    public void eliminarPaciente(Long id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            pacienteDAO.eliminar(id, conn);
        }
    }

    // =====================================================
    // ACTUALIZAR HC DIRECTAMENTE
    // =====================================================
    public void actualizarHistoria(HistoriaClinica h) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            historiaDAO.actualizar(h, conn);
        }
    }
}
