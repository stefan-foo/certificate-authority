/*
 * This file is generated by jOOQ.
 */
package rs.elfak.caprovider.db;


import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

import rs.elfak.caprovider.db.tables.Certificate;
import rs.elfak.caprovider.db.tables.CertificateRequest;
import rs.elfak.caprovider.db.tables.records.CertificateRecord;
import rs.elfak.caprovider.db.tables.records.CertificateRequestRecord;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<CertificateRecord> CERTIFICATE_ALIAS_KEY = Internal.createUniqueKey(Certificate.CERTIFICATE, DSL.name("certificate_alias_key"), new TableField[] { Certificate.CERTIFICATE.ALIAS }, true);
    public static final UniqueKey<CertificateRecord> CERTIFICATE_PKEY = Internal.createUniqueKey(Certificate.CERTIFICATE, DSL.name("certificate_pkey"), new TableField[] { Certificate.CERTIFICATE.ID }, true);
    public static final UniqueKey<CertificateRequestRecord> CERTIFICATE_REQUEST_PKEY = Internal.createUniqueKey(CertificateRequest.CERTIFICATE_REQUEST, DSL.name("certificate_request_pkey"), new TableField[] { CertificateRequest.CERTIFICATE_REQUEST.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<CertificateRecord, CertificateRecord> CERTIFICATE__CERTIFICATE_ISSUING_CERTIFICATE_FKEY = Internal.createForeignKey(Certificate.CERTIFICATE, DSL.name("certificate_issuing_certificate_fkey"), new TableField[] { Certificate.CERTIFICATE.ISSUING_CERTIFICATE }, Keys.CERTIFICATE_PKEY, new TableField[] { Certificate.CERTIFICATE.ID }, true);
    public static final ForeignKey<CertificateRequestRecord, CertificateRecord> CERTIFICATE_REQUEST__CERTIFICATE_REQUEST_CERTIFICATE_FKEY = Internal.createForeignKey(CertificateRequest.CERTIFICATE_REQUEST, DSL.name("certificate_request_certificate_fkey"), new TableField[] { CertificateRequest.CERTIFICATE_REQUEST.CERTIFICATE }, Keys.CERTIFICATE_PKEY, new TableField[] { Certificate.CERTIFICATE.ID }, true);
}