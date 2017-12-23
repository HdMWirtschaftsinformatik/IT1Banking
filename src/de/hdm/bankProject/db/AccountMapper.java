package de.hdm.bankProject.db;

import java.sql.*;
import java.util.Vector;

import de.hdm.bankProject.data.*;

/**
 * Mapper-Klasse, die <code>Account</code>-Objekte auf eine relationale
 * Datenbank abbildet. Hierzu wird eine Reihe von Methoden zur Verfuegung
 * gestellt, mit deren Hilfe z.B. Objekte gesucht, erzeugt, modifiziert und
 * geloescht werden koennen. Das Mapping ist bidirektional. D.h., Objekte
 * koennen in DB-Strukturen und DB-Strukturen in Objekte umgewandelt werden.
 * 
 * @see CustomerMapper
 * @author Thies
 */
public class AccountMapper extends DataMapper<Account> {

	/**
	 * Die Klasse AccountMapper wird nur einmal instantiiert. Man spricht hierbei
	 * von einem sogenannten <b>Singleton</b>.
	 * <p>
	 * Diese Variable ist durch den Bezeichner <code>static</code> nur einmal fuer
	 * saemtliche eventuellen Instanzen dieser Klasse vorhanden. Sie speichert die
	 * einzige Instanz dieser Klasse.
	 *
	 * @see accountMapper()
	 */
	private static AccountMapper accountMapper = null;

	/**
	 * Geschuetzter Konstruktor - verhindert die Moeglichkeit, mit new neue
	 * Instanzen dieser Klasse zu erzeugen.
	 */
	protected AccountMapper() {
	}

	/**
	 * Diese statische Methode kann aufgrufen werden durch
	 * <code>AccountMapper.accountMapper()</code>. Sie stellt die
	 * Singleton-Eigenschaft sicher, indem Sie dafuer sorgt, dass nur eine einzige
	 * Instanz von <code>AccountMapper</code> existiert.
	 * <p>
	 *
	 * <b>Fazit:</b> AccountMapper sollte nicht mittels <code>new</code>
	 * instantiiert werden, sondern stets durch Aufruf dieser statischen Methode.
	 *
	 * @return DAS <code>AccountMapper</code>-Objekt.
	 * @see accountMapper
	 */
	public static AccountMapper accountMapper() {
		if (accountMapper == null) {
			accountMapper = new AccountMapper();
		}

		return accountMapper;
	}

	public void removeTable() {
		Connection con = DBConnection.connection();
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE accounts");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void reCreateTable() {
		removeTable();
		String sqlString = "CREATE TABLE accounts" + " (" + "id int NOT NULL default 0, "
				+ "owner int NOT NULL default 0, " + "balance float NOT NULL default 0, " + "PRIMARY KEY (id) " + ") ";
		Connection con = DBConnection.connection();
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sqlString);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	public void fillTable(String dataString) {
		String sqlString = "INSERT INTO accounts (id,owner,balance) VALUES " + dataString;
		Connection con = DBConnection.connection();
		Statement stmt;
		try {
			stmt = con.createStatement();
			stmt.executeUpdate(sqlString);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Suchen eines Kontos mit vorgegebener Kontonummer. Da diese eindeutig ist,
	 * wird genau ein Objekt zurueckgegeben.
	 *
	 * @param id
	 *            Primaerschluesselattribut (->DB)
	 * @return Konto-Objekt, das dem uebergebenen Schluessel entspricht, null bei
	 *         nicht vorhandenem DB-Tupel.
	 */
	public Account findByKey(int id) {
		// DB-Verbindung holen
		Connection con = DBConnection.connection();

		try {
			// Leeres SQL-Statement (JDBC) anlegen
			Statement stmt = con.createStatement();

			// Statement ausfuellen und als Query an die DB schicken
			ResultSet rs = stmt
					.executeQuery("SELECT id, owner, balance FROM accounts " + "WHERE id=" + id + " ORDER BY owner");
			/*
			 * Da id Primaerschluessel ist, kann max. nur ein Tupel zurueckgegeben werden.
			 * Pruefe, ob ein Ergebnis vorliegt.
			 */
			if (rs.next()) {
				// Ergebnis-Tupel in Objekt umwandeln
				Account a = new Account();
				a.setId(rs.getInt("id"));
				a.setOwner(CustomerMapper.customerMapper().findByKey(rs.getInt("owner")));
				a.setBalance(rs.getFloat("balance"));
				return a;
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * Auslesen aller Konten.
	 *
	 * @return Ein Vektor mit Account-Objekten, die saemtliche Konten
	 *         repraesentieren. Bei evtl. Exceptions wird ein partiell gefuellter
	 *         oder ggf. auch leerer Vetor zurueckgeliefert.
	 */
	public Vector<Account> findAll() {
		Connection con = DBConnection.connection();

		// Ergebnisvektor vorbereiten
		Vector<Account> result = new Vector<Account>();

		try {
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT id, owner, balance FROM accounts " + " ORDER BY id");
			// Fuer jeden Eintrag im Suchergebnis wird nun ein Account-Objekt
			// erstellt.
			while (rs.next()) {
				Account a = new Account();
				a.setId(rs.getInt("id"));
				a.setOwner(CustomerMapper.customerMapper().findByKey(rs.getInt("owner")));
				a.setBalance(rs.getFloat("balance"));
				// Hinzufuegen des neuen Objekts zum Ergebnisvektor
				result.addElement(a);
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// Ergebnisvektor zurueckgeben
		return result;
	}

	/**
	 * Auslesen aller Konten eines durch Fremdschluessel (Kundennr.) gegebenen
	 * Kunden.
	 *
	 * @see findByOwner(Customer owner)
	 * @param ownerID
	 *            Schluessel des zugehoerigen Kunden.
	 * @return Ein Vektor mit Account-Objekten, die saemtliche Konten des
	 *         betreffenden Kunden repraesentieren. Bei evtl. Exceptions wird ein
	 *         partiell gefuellter oder ggf. auch leerer Vetor zurueckgeliefert.
	 */
	public Vector<Account> findByOwner(int ownerID) {
		Connection con = DBConnection.connection();
		Vector<Account> result = new Vector<Account>();

		try {
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery(
					"SELECT id, owner, balance FROM accounts " + "WHERE owner=" + ownerID + " ORDER BY id");

			// Fuer jeden Eintrag im Suchergebnis wird nun ein Account-Objekt
			// erstellt.
			while (rs.next()) {
				Account a = new Account();
				a.setId(rs.getInt("id"));
				a.setOwner(CustomerMapper.customerMapper().findByKey(rs.getInt("owner")));
				a.setBalance(rs.getFloat("balance"));

				// Hinzufuegen des neuen Objekts zum Ergebnisvektor
				result.addElement(a);
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// Ergebnisvektor zurueckgeben
		return result;
	}

	/**
	 * Auslesen aller Konten eines Kunden (durch <code>Customer</code>-Objekt
	 * gegeben).
	 *
	 * @see findByOwner(int ownerID)
	 * @param owner
	 * @return
	 */
	public Vector<Account> findByOwner(Customer owner) {
		/*
		 * Wir lesen einfach die Kundennummer (Primaerschluessel) des Customer-Objekts
		 * aus und delegieren die weitere Bearbeitung an findByOwner(int ownerID).
		 */
		return findByOwner(owner.getId());
	}

	/**
	 * Einfuegen eines <code>Account</code>-Objekts in die Datenbank. Dabei wird
	 * auch der Primaerschluessel des uebergebenen Objekts geprueft und ggf.
	 * berichtigt.
	 *
	 * @param a
	 *            das zu speichernde Objekt
	 * @return das bereits uebergebene Objekt, jedoch mit ggf. korrigierter
	 *         <code>id</code>.
	 */
	public Account insert(Account a) {
		Connection con = DBConnection.connection();

		try {
			Statement stmt = con.createStatement();

			/*
			 * Zunaechst schauen wir nach, welches der momentan hoechste
			 * Primaerschluesselwert ist.
			 */
			ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS maxid " + "FROM accounts ");

			// Wenn wir etwas zurueckerhalten, kann dies nur einzeilig sein
			if (rs.next()) {
				/*
				 * a erhaelt den bisher maximalen, nun um 1 inkrementierten Primaerschluessel.
				 */
				a.setId(rs.getInt("maxid") + 1);
				stmt = con.createStatement();
				// Jetzt erst erfolgt die tatsaechliche Einfuegeoperation
				stmt.executeUpdate("INSERT INTO accounts (id, owner, balance) " + "VALUES (" + a.getId() + ","
						+ a.getOwner().getId() + "," + a.getBalance() + ")");
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		/*
		 * Rueckgabe, des evtl. korrigierten Accounts.
		 *
		 * HINWEIS: Da in Java nur Referenzen auf Objekte und keine physischen Objekte
		 * uebergeben werden, waere die Anpassung des Account-Objekts auch ohne diese
		 * explizite Rueckgabe ausserhalb dieser Methode sichtbar. Die explizite
		 * Rueckgabe von a ist eher ein Stilmittel, um zu signalisieren, dass sich das
		 * Objekt evtl. im Laufe der Methode veraendert hat.
		 */
		return a;
	}

	/**
	 * Wiederholtes Schreiben eines Objekts in die Datenbank.
	 *
	 * @param a
	 *            das Objekt, das in die DB geschrieben werden soll
	 * @return das als Parameter uebergebene Objekt
	 */
	public Account update(Account a) {
		Connection con = DBConnection.connection();

		try {
			Statement stmt = con.createStatement();
			String updateString = "UPDATE accounts " + "SET owner=" + a.getOwner().getId() + ", " + "balance="
					+ a.getBalance() + " " + "WHERE id=" + a.getId();
			stmt.executeUpdate(updateString);
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		// Um Analogie zu insert(Account a) zu wahren, geben wir a zurueck
		return a;
	}

	/**
	 * Loeschen der Daten eines <code>Account</code>-Objekts aus der Datenbank.
	 *
	 * @param a
	 *            das aus der DB zu loeschende "Objekt"
	 */
	public void delete(Account a) {
		Connection con = DBConnection.connection();
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("DELETE FROM accounts " + "WHERE id=" + a.getId());
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
	}
}
