/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2014 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package keggapi;

/**
 * We are keeping a single copy of this keggapi file to make our applications
 * compatible with stored binary caches.
 * 
 * The KEGG API changed on 2013-01-01 to a REST interface and the previous JAR
 * is not required anymore. Even usage of this class should be avoided in the
 * future.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
@Deprecated
public class Definition  implements java.io.Serializable {
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 4715250871472887071L;
  
  private java.lang.String entry_id;
  
  private java.lang.String definition;
  
  public Definition() {
  }
  
  public Definition(
    java.lang.String entry_id,
    java.lang.String definition) {
    this.entry_id = entry_id;
    this.definition = definition;
  }
  
  
  /**
   * Gets the entry_id value for this Definition.
   * 
   * @return entry_id
   */
  public java.lang.String getEntry_id() {
    return entry_id;
  }
  
  
  /**
   * Sets the entry_id value for this Definition.
   * 
   * @param entry_id
   */
  public void setEntry_id(java.lang.String entry_id) {
    this.entry_id = entry_id;
  }
  
  
  /**
   * Gets the definition value for this Definition.
   * 
   * @return definition
   */
  public java.lang.String getDefinition() {
    return definition;
  }
  
  
  /**
   * Sets the definition value for this Definition.
   * 
   * @param definition
   */
  public void setDefinition(java.lang.String definition) {
    this.definition = definition;
  }
  
  private java.lang.Object __equalsCalc = null;
  @Override
  public synchronized boolean equals(java.lang.Object obj) {
    if (!(obj instanceof Definition)) {
      return false;
    }
    Definition other = (Definition) obj;
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true &&
        ((entry_id==null && other.getEntry_id()==null) ||
            (entry_id!=null &&
            entry_id.equals(other.getEntry_id()))) &&
            ((definition==null && other.getDefinition()==null) ||
                (definition!=null &&
                definition.equals(other.getDefinition())));
    __equalsCalc = null;
    return _equals;
  }
  
  private boolean __hashCodeCalc = false;
  @Override
  public synchronized int hashCode() {
    if (__hashCodeCalc) {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getEntry_id() != null) {
      _hashCode += getEntry_id().hashCode();
    }
    if (getDefinition() != null) {
      _hashCode += getDefinition().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }
  
  // メタデータ型 / [en]-(Type metadata)
  private static org.apache.axis.description.TypeDesc typeDesc =
  new org.apache.axis.description.TypeDesc(Definition.class, true);
  
  static {
    typeDesc.setXmlType(new javax.xml.namespace.QName("SOAP/KEGG", "Definition"));
    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("entry_id");
    elemField.setXmlName(new javax.xml.namespace.QName("", "entry_id"));
    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new org.apache.axis.description.ElementDesc();
    elemField.setFieldName("definition");
    elemField.setXmlName(new javax.xml.namespace.QName("", "definition"));
    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
  }
  
  /**
   * メタデータオブジェクトの型を返却 / [en]-(Return type metadata object)
   */
  public static org.apache.axis.description.TypeDesc getTypeDesc() {
    return typeDesc;
  }
  
  /**
   * Get Custom Serializer
   */
  public static org.apache.axis.encoding.Serializer getSerializer(
    java.lang.String mechType,
    java.lang.Class _javaType,
    javax.xml.namespace.QName _xmlType) {
    return
        new  org.apache.axis.encoding.ser.BeanSerializer(
          _javaType, _xmlType, typeDesc);
  }
  
  /**
   * Get Custom Deserializer
   */
  public static org.apache.axis.encoding.Deserializer getDeserializer(
    java.lang.String mechType,
    java.lang.Class _javaType,
    javax.xml.namespace.QName _xmlType) {
    return
        new  org.apache.axis.encoding.ser.BeanDeserializer(
          _javaType, _xmlType, typeDesc);
  }
  
}

