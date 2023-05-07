package lsi.ubu.solucion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.enunciado.GestionMedicosException;
import lsi.ubu.util.ExecuteScript;
import lsi.ubu.util.PoolDeConexiones;


/**
 * GestionMedicos:
 * Implementa la gestion de medicos segun el PDF de la carpeta enunciado
 * 
 * @author <a href="mailto:jmaudes@ubu.es">Jesus Maudes</a>
 * @author <a href="mailto:rmartico@ubu.es">Raul Marticorena</a>
 * @author <a href="mailto:pgdiaz@ubu.es">Pablo Garcia</a>
 * @author <a href="mailto:alu.ubu.es">Jose Carlos Chico Mena</a>
 * @version 1.0
 * @since 1.0 
 */
public class GestionMedicos {
	
	private static Logger logger = LoggerFactory.getLogger(GestionMedicos.class);

	private static final String script_path = "sql/";

	public static void main(String[] args) throws SQLException{	
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		tests();
		ver_medicos();
		try {
			//reservar_consulta("87654321B", "222222B",  formato.parse("24/03/2023"));
			reservar_consulta("78677433R", "8766788Y",  formato.parse("30/04/2023"));
		} catch (SQLException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		consulta_medico("8766788Y");

		System.out.println("FIN.............");
	}
	public static void ver_medicos()
			throws SQLException {
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			System.out.println();
			System.out.println("Conectados... Iniciando ejecucion de 'ver_medicos' ");
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SElECT * FROM medico");
			
			System.out.println("ID_MEDICO"+'\t'+"NIF"+'\t'+"NOMBRE"+'\t'+"APE1"+'\t'+"APE2"+'\t'+"ESPECIALIDAD"+'\t'+"CONSULTAS");
			while (rs.next()) { 
				System.out.println(
					rs.getString(1)+'\t'+rs.getString(2)+'\t'+rs.getString(3)+'\t'+rs.getString(4)+'\t'+
					rs.getString(5)+'\t'+rs.getString(6)+'\t'+rs.getString(7));
			}
			
			rs.close();
			st.close();
			con.close();
			
			System.out.println("Cerrada la conexión y saliendo...");
			
		} catch (SQLException e) {
			//Completar por el alumno
			System.err.println(e.getMessage());
			e.printStackTrace();
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
			con.close();
		}
		
	}
	
	/*@ID_MEDICO 
	@Column(name="ID", unique = true, nullable = false) 
	@**GeneratedValue(strategy=GenerationType.SEQUENCE)**      */
	public static void reservar_consulta(String m_NIF_cliente, 
			String m_NIF_medico,  Date m_Fecha_Consulta) throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			System.out.println();
			logger.info("\"Conectados... Iniciando ejecucion de 'reservar_consulta' \"");
			System.out.println("Conectados... Iniciando ejecucion de 'reservar_consulta' ");
			
			//La funcion nos proporciona el NIF del medico pero la tabla consulta introduce el ID_MEDICO de ese NIF
			//en las siguientes lineas realizamos la consulta para obtener el identificador ID_MEDICO del NIF del medico variable 'm_NIF_medico'
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT ID_MEDICO FROM medico WHERE NIF='"+m_NIF_medico+"'");
			/*
			 Si el NIF del medico no existe en la tabla medico lanzará una excepción
			GestionMedicosException, con el código 2, y el mensaje “Medico inexistente”.
			 */
			boolean aux = rs.next();
			if (!aux)
				 throw new GestionMedicosException(GestionMedicosException.MEDICO_NO_EXISTE);	
			logger.info("Localizado ID_MEDICO="+'\t'+ aux);
			System.out.println("Localizado ID_MEDICO="+'\t'+ aux);
			Integer identificador_medico=rs.getInt(1);
			
			
			
			//La funcion nos proporciona el NIF del cliente pero puede ocurrir que el cliente no exista o se teclee mal el NIF
			//en las siguientes lineas realizamos la consulta para obtener el registro con el NIF del cliente variable 'm_NIF_cliente'
			//Statement st = con.createStatement();
			rs = st.executeQuery("SELECT NOMBRE FROM cliente WHERE NIF='"+m_NIF_cliente+"'");
			/*
			Si el NIF del cliente no existe en la tabla de clientes lanzará una excepción
			GestionMedicosException, con el código 1, y el mensaje “Cliente inexistente”.
			*/
			aux = rs.next();
			if (!aux)
				 throw new GestionMedicosException(GestionMedicosException.CLIENTE_NO_EXISTE);	
			String nombre_cliente=rs.getString(1);
			logger.info("Localizado cliente con NIF="+'\t'+ m_NIF_cliente+" con nombre= "+nombre_cliente+" Comprobacion="+aux);
			System.out.println("Localizado cliente con NIF="+'\t'+ m_NIF_cliente+" con nombre= "+nombre_cliente+" Comprobacion="+aux);
			
			
			
			

			 
			
			//Realizamos la introduccion de datos con PreparedStatement, seteando los parametros a introducir e introduciendolos con
			//executeUpdate, de ir todo bien el ResultSet rs2 devolvera un 1
			PreparedStatement pst = con.prepareStatement("INSERT INTO consulta VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			//la fecha proporcionada mediante un dato java.util.Date la transformamos en java.sql.Date para poder introducirla
			//directamente en el PreparedStatement con su seDate correspondiente
			SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
			java.sql.Date sqlDate = java.sql.Date.valueOf(m_Fecha_Consulta.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			pst.setInt(1,3);
			pst.setDate(2,sqlDate);
			pst.setString(3,String.valueOf(identificador_medico));
			pst.setString(4,m_NIF_cliente);
			Integer rs2 = pst.executeUpdate();
			boolean b = (rs2 != 0);
			logger.info("Consulta reservada"+'\t'+ b);
			System.out.println("Consulta reservada"+'\t'+ b);
			//Captura del indice generado en la introduccion de los datos
			/*ResultSet res = pst.getGeneratedKeys();
			int id = -1;
			if(res.next()) {
			    id = res.getInt(1);
			}			
			System.out.println("Indice del nuevo registro="+id);*/
			
			//Recogida de datos desde la tabla actualizada para ratificar el exito de la operacion y dar esa informacion al usuario
			//Statement st3 = con.createStatement();
			logger.info("Fecha enviada="+'\t'+'\t'+String.valueOf(sqlDate));
			System.out.println("Fecha enviada="+'\t'+'\t'+String.valueOf(sqlDate));
			ResultSet rs3 = st.executeQuery(    "SELECT * FROM consulta WHERE FECHA_CONSULTA=DATE '"+String.valueOf(sqlDate)+"'"    );
			
			
			logger.info("DATOS INTRODUCIDOS EN TABLA CONSULTA");
			System.out.println("DATOS INTRODUCIDOS EN TABLA CONSULTA");	
			logger.info("ID_CONSULTA"+'\t'+"FECHA_CONSULTA"+'\t'+'\t'+"ID_MEDICO"+'\t'+"NIF Cliente");
			System.out.println("ID_CONSULTA"+'\t'+"FECHA_CONSULTA"+'\t'+'\t'+"ID_MEDICO"+'\t'+"NIF Cliente");			
			while (rs3.next()) { 
				logger.info(rs3.getString(1)+'\t'+'\t'+rs3.getString(2).trim()+'\t'+rs3.getString(3)+'\t'+'\t'+rs3.getString(4));
				System.out.println(
					rs3.getString(1)+'\t'+'\t'+rs3.getString(2).trim()+'\t'+rs3.getString(3)+'\t'+'\t'+rs3.getString(4));
			}			
			
			
			rs.close();
			st.close();
			
			pst.close();
			
			rs3.close();
			
			con.close();
			
			logger.info("Cerrada la conexión y saliendo...");
			System.out.println("Cerrada la conexión y saliendo...");
			
		} catch (SQLException e) {
			//Completar por el alumno
			con.rollback();
			System.out.println(e.getMessage());
			System.out.println("Codigo de Error Oracle: " + e.getErrorCode());
			System.out.println("Transaccion retrocedida probablemente "+
			 "Médico inexistente o Cliente inexistente.");
			
			System.err.println(e.getMessage());
			e.printStackTrace();
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
			con.close();
			
		}		
		
		
		
	}
	
	public static void anular_consulta(String m_NIF_cliente, String m_NIF_medico,  
			Date m_Fecha_Consulta, Date m_Fecha_Anulacion)
			throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			
		} catch (SQLException e) {
			//Completar por el alumno			
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
		}		
	}
	
	public static void consulta_medico(String m_NIF_medico)
			throws SQLException {

				
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

	
		try{
			con = pool.getConnection();
			System.out.println();
			System.out.println("Conectados... Iniciando ejecucion de 'consulta_medico' ");
			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT ID_MEDICO FROM medico WHERE NIF='"+m_NIF_medico+"'");
			boolean aux = rs.next();
			System.out.println("Localizado ID_MEDICO="+'\t'+ aux);
			Integer identificador_medico=rs.getInt(1);
			
			//Statement st2 = con.createStatement();
			ResultSet rs2 = st.executeQuery(    "SELECT * FROM consulta WHERE ID_MEDICO='"+String.valueOf(identificador_medico)+"' ORDER BY FECHA_CONSULTA"    );
			System.out.println("CONSULTAS del MEDICO CON NIF="+m_NIF_medico);	
			System.out.println("ID_CONSULTA"+'\t'+"FECHA_CONSULTA"+'\t'+'\t'+"ID_MEDICO"+'\t'+"NIF cliente");			
			while (rs2.next()) { 
				System.out.println(
					rs2.getString(1)+'\t'+'\t'+rs2.getString(2)+'\t'+rs2.getString(3)+'\t'+'\t'+rs2.getString(4));
			}
			rs.close();
			st.close();
			con.close();
			
			System.out.println("Cerrada la conexión y saliendo...");
			
		} catch (SQLException e) {
			//Completar por el alumno			
			
			logger.error(e.getMessage());
			throw e;		

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
		}		
	}
	
	static public void creaTablas() {
		ExecuteScript.run(script_path + "gestion_medicos.sql");
	}

	static void tests() throws SQLException{
		creaTablas();
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();		
		
		//Relatar caso por caso utilizando el siguiente procedure para inicializar los datos
		
		CallableStatement cll_reinicia=null;
		Connection conn = null;
		
		try {
			//Reinicio filas
			conn = pool.getConnection();
			cll_reinicia = conn.prepareCall("{call inicializa_test}");
			cll_reinicia.execute();
		} catch (SQLException e) {				
			logger.error(e.getMessage());			
		} finally {
			if (cll_reinicia!=null) cll_reinicia.close();
			if (conn!=null) conn.close();
		
		}			
		
	}
}