/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.meta;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;

/**
 * Default {@link ValueMetaData} implementation.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ValueMetaDataImpl
    implements ValueMetaData {

    private static final Localizer _loc = Localizer.forPackage
        (ValueMetaDataImpl.class);

    ///////////////////////////////////////////////////////////////
    // Note: if you add additional state that should be copied to
    // embedded metadata, make sure to add it to the copy() method
    ///////////////////////////////////////////////////////////////

    private FieldMetaData _owner;
    private Class _decType = Object.class;
    private int _decCode = JavaTypes.OBJECT;
    private ClassMetaData _decTypeMeta = null;
    private Class _type = null;
    private int _code = JavaTypes.OBJECT;
    private ClassMetaData _typeMeta = null;
    private Class _typeOverride = null;
    private int _delete = CASCADE_NONE;
    private int _persist = CASCADE_AUTO;
    private int _attach = CASCADE_IMMEDIATE;
    private int _detach = CASCADE_AUTO;
    private int _refresh = CASCADE_AUTO;
    private boolean _serialized = false;
    private Boolean _embedded = null;
    private ClassMetaData _embeddedMeta = null;
    private int _resMode = MODE_NONE;
    private String _mappedBy = null;
    private FieldMetaData _mappedByMeta = null;

    protected ValueMetaDataImpl(FieldMetaData owner) {
        _owner = owner;
    }
    
    /**
     * Constructor for serialization.
     */
    protected ValueMetaDataImpl() {
    }

    public FieldMetaData getFieldMetaData() {
        return _owner;
    }

    public MetaDataRepository getRepository() {
        return _owner.getRepository();
    }

    public Class getType() {
        return (_type == null) ? _decType : _type;
    }

    public void setType(Class type) {
        _type = type;
        _typeMeta = null;
        if (type != null)
            setTypeCode(JavaTypes.getTypeCode(type));
    }

    public int getTypeCode() {
        return (_type == null) ? _decCode : _code;
    }

    public void setTypeCode(int code) {
        _code = code;
    }

    public boolean isTypePC() {
        return getTypeCode() == JavaTypes.PC
            || getTypeCode() == JavaTypes.PC_UNTYPED;
    }

    public ClassMetaData getTypeMetaData() {
        if (_type == null)
            return getDeclaredTypeMetaData();
        if (_typeMeta == null && _code == JavaTypes.PC) {
            ClassMetaData meta = _owner.getDefiningMetaData();
            _typeMeta = meta.getRepository().getMetaData(_type,
                meta.getEnvClassLoader(), true);
        }
        return _typeMeta;
    }

    public Class getDeclaredType() {
        return _decType;
    }

    public void setDeclaredType(Class type) {
        _decType = type;
        _decTypeMeta = null;
        _decCode = JavaTypes.getTypeCode(type);
        if (_embeddedMeta != null)
            _embeddedMeta.setDescribedType(type);
    }

    public int getDeclaredTypeCode() {
        return _decCode;
    }

    public void setDeclaredTypeCode(int code) {
        _decCode = code;
    }

    public boolean isDeclaredTypePC() {
        return _decCode == JavaTypes.PC || _decCode == JavaTypes.PC_UNTYPED;
    }

    public ClassMetaData getDeclaredTypeMetaData() {
        if (_decTypeMeta == null && _decCode == JavaTypes.PC) {
            if (isEmbedded())
                _decTypeMeta = getEmbeddedMetaData();
            else {
                ClassMetaData meta = _owner.getDefiningMetaData();
                _decTypeMeta = meta.getRepository().getMetaData(_decType,
                    meta.getEnvClassLoader(), true);
            }
        }
        return _decTypeMeta;
    }

    public boolean isEmbedded() {
        if (_owner.getManagement() != _owner.MANAGE_PERSISTENT)
            return false;
        if (_embedded == null) {
            // field left as default; embedded setting depends on type
            switch (_decCode) {
                case JavaTypes.PC:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                case JavaTypes.PC_UNTYPED:
                    _embedded = Boolean.FALSE;
                    break;
                default:
                    _embedded = Boolean.TRUE;
            }
        }
        return _embedded.booleanValue();
    }

    public void setEmbedded(boolean embedded) {
        if (embedded && _embedded != Boolean.TRUE) {
            _decTypeMeta = null;
            _typeMeta = null;
        }
        _embedded = (embedded) ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean isEmbeddedPC() {
        return _decCode == JavaTypes.PC && isEmbedded();
    }

    public ClassMetaData getEmbeddedMetaData() {
        if (_embeddedMeta == null && isEmbeddedPC())
            addEmbeddedMetaData();
        return _embeddedMeta;
    }
   
    public ClassMetaData addEmbeddedMetaData(int access) {
        MetaDataRepository repos = _owner.getRepository();
        _embeddedMeta = repos.newEmbeddedClassMetaData(this);
        _embeddedMeta.setDescribedType(_decType);
        repos.getMetaDataFactory().getDefaults().populate(_embeddedMeta,
                access);

        setEmbedded(true);
        return _embeddedMeta;
    }

    public ClassMetaData addEmbeddedMetaData() {
        return addEmbeddedMetaData(AccessCode.UNKNOWN);
    }

    public int getCascadeDelete() {
        if (_owner.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            return CASCADE_NONE;
        if (isEmbeddedPC())
            return CASCADE_IMMEDIATE;

        switch (_delete) {
            case CASCADE_NONE:
                // if the user marks the owning field dependent and we 
                // externalize to a pc type, then become dependent
                if (this != _owner.getValue() && isTypePC()
                    && ((ValueMetaDataImpl) _owner.getValue())._delete
                    == CASCADE_AUTO)
                    return CASCADE_AUTO;
                break;
            case CASCADE_AUTO:
                if (isTypePC())
                    return CASCADE_AUTO;
                break;
            case CASCADE_IMMEDIATE:
                if (isDeclaredTypePC())
                    return CASCADE_IMMEDIATE;
                break;
        }
        return CASCADE_NONE;
    }

    public void setCascadeDelete(int delete) {
        _delete = delete;
    }

    public int getCascadePersist() {
        if (_owner.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            return CASCADE_NONE;
        if (isDeclaredTypePC())
            return _persist;
        if (!isTypePC())
            return CASCADE_NONE;
        // if only externalized type is pc, can't cascade immediate
        return (_persist == CASCADE_IMMEDIATE) ? CASCADE_AUTO : _persist;
    }

    public void setCascadePersist(int persist) {
        _persist = persist;
    }

    public int getCascadeAttach() {
        if (_owner.getManagement() != FieldMetaData.MANAGE_PERSISTENT
            || !isDeclaredTypePC()) // attach acts on declared type
            return CASCADE_NONE;
        if (isEmbeddedPC())
            return CASCADE_IMMEDIATE;
        return _attach;
    }

    public void setCascadeAttach(int attach) {
        if (attach == CASCADE_AUTO)
            throw new IllegalArgumentException("CASCADE_AUTO");
        _attach = attach;
    }

    public int getCascadeDetach() {
        if (_owner.getManagement() != FieldMetaData.MANAGE_PERSISTENT
                || !isDeclaredTypePC()) // detach acts on declared type
                return CASCADE_NONE;
        if (isEmbedded())
            return CASCADE_IMMEDIATE;
        return _detach;
    }

    public void setCascadeDetach(int detach) {
        _detach = detach;
    }

    public int getCascadeRefresh() {
        if (_owner.getManagement() != FieldMetaData.MANAGE_PERSISTENT
            || !isDeclaredTypePC()) // refresh acts on declared type
            return CASCADE_NONE;
        return _refresh;
    }

    public void setCascadeRefresh(int refresh) {
        _refresh = refresh;
    }

    public boolean isSerialized() {
        return _serialized;
    }

    public void setSerialized(boolean serialized) {
        _serialized = serialized;
    }

    public String getValueMappedBy() {
        if (_mappedBy == MAPPED_BY_PK) {
            // use this instead of getting meta from element b/c that
            // requires element to be resolved
            ClassMetaData meta = getRepository().getMetaData
                (_owner.getElement().getType(), null, false);
            if (meta == null)
                throw new MetaDataException(_loc.get("val-not-pc", _owner));
            if (meta.getPrimaryKeyFields().length != 1)
                throw new MetaDataException(_loc.get("val-not-one-pk",
                    _owner));
            _mappedByMeta = meta.getPrimaryKeyFields()[0];
            _mappedBy = _mappedByMeta.getName();
        }
        return _mappedBy;
    }

    public void setValueMappedBy(String mapped) {
        if (_owner.getKey() != this && mapped != null)
            throw new UserException(_loc.get("mapped-by-not-key", this));
        else {
            _mappedBy = mapped;
            _mappedByMeta = null;
        }
    }

    public FieldMetaData getValueMappedByMetaData() {
        if (getValueMappedBy() != null && _mappedByMeta == null) {
            ClassMetaData meta = _owner.getElement().getTypeMetaData();
            FieldMetaData field = (meta == null) ? null
                : meta.getField(getValueMappedBy());
            if (field == null)
                throw new MetaDataException(_loc.get("no-mapped-by", this,
                    getValueMappedBy()));
            if (field.getMappedBy() != null)
                throw new MetaDataException(_loc.get("circ-mapped-by", this,
                    getValueMappedBy()));
            _mappedByMeta = field;
        }
        return _mappedByMeta;
    }

    public Class getTypeOverride() {
        return _typeOverride;
    }

    public void setTypeOverride(Class val) {
        _typeOverride = val;
    }

    public String toString() {
        String ret = _owner.getFullName(true);
        if (this == _owner.getKey())
            return ret + "<key:" + _decType + ">";
        if (this == _owner.getElement()) {
            if (_owner.getTypeCode() == JavaTypes.MAP)
                return ret + "<value:" + _decType + ">";
            return ret + "<element:" + _decType + ">";
        }
        return ret + "<" + _decType + ">";
    }

    ////////////////////////
    // Resolve and validate
    ////////////////////////

    public int getResolve() {
        return _resMode;
    }

    public void setResolve(int mode) {
        _resMode = mode;
    }

    public void setResolve(int mode, boolean on) {
        if (mode == MODE_NONE)
            _resMode = mode;
        else if (on)
            _resMode |= mode;
        else
            _resMode &= ~mode;
    }

    public boolean resolve(int mode) {
        if ((_resMode & mode) == mode)
            return true;
        int cur = _resMode;
        _resMode |= mode;

        // we only perform actions for meta mode
        if ((mode & MODE_META) == 0 || (cur & MODE_META) != 0)
            return false;

        // check for type extension
        int codeOverride = JavaTypes.OBJECT;
        if (_typeOverride != null) {
            codeOverride = JavaTypes.getTypeCode(_typeOverride);

            // if there is no externalizer method or this value is a key or
            // element, set our type to the type extension; otherwise, use the
            // type extension as a hint to the actual type of the declared
            // value (e.g. marking an interface as non-pc)
            if (_owner.getExternalizerMethod() == null
                || _owner.getValue() != this) {
                _type = _typeOverride;
                _code = codeOverride;
            } else {
                _decCode = codeOverride;
                if (JavaTypes.maybePC(codeOverride, _typeOverride))
                    resolveDeclaredType(_typeOverride);
            }
        }

        // see if actual type is pc
        if (JavaTypes.maybePC(_code, _type)) {
            _typeMeta = _owner.getRepository().getMetaData(_type,
                _owner.getDefiningMetaData().getEnvClassLoader(), false);
            if (_typeMeta != null)
                _code = JavaTypes.PC;
        }

        // if there is no externalizer, set our declared type code to the
        // actual type so that we treat the value correctly at runtime
        // (pers by reach, etc)
        if (_typeOverride != null && _owner.getExternalizerMethod() == null
            && _owner.getExternalValues() == null) {
            // cache the metadata immediately since we won't be able to get
            // it lazily, since we're not resetting _decType to _type
            _decCode = _code;
            _decTypeMeta = _typeMeta;
        } else if (JavaTypes.maybePC(_decCode, _decType))
            resolveDeclaredType(_decType);

        // resolves mapped by
        getValueMappedBy();

        ClassMetaData embed = getEmbeddedMetaData();
        if (embed != null)
            embed.resolve(MODE_META);

        // oid as primary key field?
        if (_decCode == JavaTypes.PC && isEmbedded()
            && _owner.isPrimaryKey() && _owner.getValue() == this)
            _code = _decCode = JavaTypes.OID;

        return false;
    }

    /**
     * Resolve the declared type.
     */
    private void resolveDeclaredType(Class type) {
        ClassMetaData meta = _owner.getRepository().getMetaData(type,
            _owner.getDefiningMetaData().getEnvClassLoader(), false);
        if (meta != null)
            _decCode = JavaTypes.PC;
        
        if (meta != null && meta.isEmbeddedOnly() && !meta.isAbstract())
            setEmbedded(true);
                
        if (!isEmbedded()) 
            _decTypeMeta = meta;
    }

    public void copy(ValueMetaData vmd) {
        // copy declared types, but if OID revert to PC until we resolve
        // to OID ourselves
        _decType = vmd.getDeclaredType();
        _decCode = vmd.getDeclaredTypeCode();
        if (_decCode == JavaTypes.OID)
            _decCode = JavaTypes.PC;

        _delete = vmd.getCascadeDelete();
        _persist = vmd.getCascadePersist();
        _attach = vmd.getCascadeAttach();
        _detach = vmd.getCascadeDetach();
        _refresh = vmd.getCascadeRefresh();
        _typeOverride = vmd.getTypeOverride();
        _serialized = vmd.isSerialized();
        if (_embeddedMeta != null)
            _embeddedMeta.setDescribedType(vmd.getDeclaredType());

        // don't allow copy to override embedded
        if (_embedded == null)
            setEmbedded(vmd.isEmbedded());
    }
}
