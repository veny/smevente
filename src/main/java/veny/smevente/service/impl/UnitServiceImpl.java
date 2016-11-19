package veny.smevente.service.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import eu.maydu.gwt.validation.client.server.ServerValidation;
import veny.smevente.dao.CustomerDao;
import veny.smevente.dao.ProcedureDao;
import veny.smevente.dao.UnitDao;
import veny.smevente.misc.Utensils;
import veny.smevente.model.Customer;
import veny.smevente.model.Event;
import veny.smevente.model.Procedure;
import veny.smevente.model.Unit;
import veny.smevente.server.validation.ValidationContainer;
import veny.smevente.service.UnitService;

/**
 * Implementation of service collecting methods associated to Unit.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 4.7.2010
 */
public class UnitServiceImpl implements UnitService {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UnitServiceImpl.class.getName());

    /** Dependency. */
    @Autowired
    private UnitDao unitDao;
    /** Dependency. */
    @Autowired
    private CustomerDao customerDao;
    /** Dependency. */
    @Autowired
    private ProcedureDao procedureDao;
    /** Dependency. */
    @Autowired
    private ValidationContainer validationContainer;


    // ------------------------------------------------------------- Unit Stuff

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Unit storeUnit(final Unit unit) {
        validateUnit(unit);

        final String name = unit.getName();
        final List<Unit> check = unitDao.findBy("name", name, null);
        if (!check.isEmpty()
                && (null == unit.getId()
                || (null != unit.getId() && !unit.getId().equals(check.get(0).getId())))) {
            throw new IllegalStateException("unit with given name already exists, name=" + name);
        }

        final Unit rslt = unitDao.persist(unit);
        LOG.info((null == unit.getId() ? "created new unit, " : "unit updated, ") + rslt);
        return rslt;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Unit getUnit(final Object id) {
        final Unit unit = unitDao.getById(id);
        LOG.info("found unit by id=" + id);
        return unit;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Unit> getAllUnits() {
        final List<Unit> found = unitDao.getAll();
        LOG.info("found all units, size=" + found.size());
        return found;
    }

    // --------------------------------------------------------- Customer Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Customer storeCustomer(final Customer client) {
        if (null == client.getUnit() || null == client.getUnit().getId()) {
            throw new NullPointerException("unknown unit");
        }
        final Unit unit = unitDao.getById(client.getUnit().getId());
        client.setUnit(unit);

        validateCustomer(client);

        // unique birth number
        if (!Utensils.stringIsBlank(client.getBirthNumber())) {
            final Customer bn = customerDao.findByBirthNumber(client.getUnit().getId(), client.getBirthNumber());
            if (null == bn || (null != client.getId() && bn.getId().toString().equals(client.getId().toString()))) {
                // expected state <- birth number not found
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("duplicate birth number check OK, bn=" + client.getBirthNumber());
                }
            } else {
                ServerValidation.exception("duplicateValue", "birthNumber", (Object[]) null);
            }
        }

        final Customer rslt = customerDao.persist(client);
        LOG.info((null == client.getId() ? "created new client, " : "client updated, ") + rslt);
        return rslt;
    }


    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Customer getCustomerById(final Object id) {
        final Customer rslt = customerDao.getById(id);
        return rslt;
    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Customer> getCustomersByUnit(final Object unitId) {
        if (null == unitId) {
            throw new NullPointerException("unit ID cannot be null");
        }
        final List<Customer> rslt = customerDao.findBy("unit", unitId, null);
        LOG.info("found " + rslt.size() + " customer(s) by unit");
        return rslt;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Customer> findCustomers(
            final Object unitId, final String name, final String phoneNumber, final String birthNumber) {

        if (null == unitId) {
            throw new NullPointerException("unit ID cannot be null");
        }
        LOG.info("findCustomers, unitId=" + unitId + ", name=" + name
                + ", phone=" + phoneNumber + ", birthNumber=" + birthNumber);

        if (null == name && null == phoneNumber && null == birthNumber) {
            return getCustomersByUnit(unitId);
        }

        List<Customer> collectedRslt = null;

        // name
        if (null != name) {
            collectedRslt = customerDao.findLikeBy(unitId, "asciiFullname", name.toUpperCase());
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("customer(s) found by name, size=" + collectedRslt.size());
            }
        }

        // phone number
        if (null != phoneNumber) {
            final List<Customer> found = customerDao.findLikeBy(unitId, "phoneNumber", phoneNumber);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("customer(s) found by phone number, size=" + found.size());
            }
            if (null == collectedRslt) {
                collectedRslt = found;
            } else {
                collectedRslt = (List<Customer>) CollectionUtils.intersection(collectedRslt, found);
            }
        }

        // birth number
        if (null != birthNumber) {
            final List<Customer> found = customerDao.findLikeBy(unitId, "birthNumber", birthNumber);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("customer(s) found by birth number, size=" + found.size());
            }
            if (null == collectedRslt) {
                collectedRslt = found;
            } else {
                collectedRslt = (List<Customer>) CollectionUtils.intersection(collectedRslt, found);
            }
        }

        LOG.info("customer(s) found, size=" + collectedRslt.size());
        return collectedRslt;
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void deleteCustomer(final Object id) {
        customerDao.remove(id);
        LOG.info("customer deleted, id=" + id);
    }

    // -------------------------------------------------------- Procedure Stuff

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public Procedure storeProcedure(final Procedure proc) {
        if (null == proc.getUnit() || null == proc.getUnit().getId()) {
            throw new NullPointerException("unknown unit");
        }
        final Unit unit = unitDao.getById(proc.getUnit().getId());
        proc.setUnit(unit);

        validateProcedure(proc);

        // unique name and type
        Procedure foundWithEqualName = null;
        try {
            foundWithEqualName =
                    procedureDao.findByNameAndType(proc.getUnit().getId(), proc.getName(), proc.enumType());
        } catch (IllegalStateException e) {
            // expected state <- combination of name and type not found
            foundWithEqualName = null;
        }
        // new procedure
        if (null != foundWithEqualName && null == proc.getId()) {
            ServerValidation.exception("duplicateValue", "name", (Object[]) null);
        }
        // existing procedure can have the same name
        if (null != foundWithEqualName && null != proc.getId()
                && !proc.getId().toString().equals(foundWithEqualName.getId().toString())) {
            ServerValidation.exception("duplicateValue", "name", (Object[]) null);
        }

        final Procedure rslt = procedureDao.persist(proc);
        LOG.info((null == proc.getId() ? "created new procedure, " : "procedure updated, ") + rslt);
        return rslt;
    }

//    /** {@inheritDoc} */
//    @Transactional
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public void updateMedicalHelpCategory(final MedicalHelpCategoryDto mhc) {
//        if (null == mhc.getId()) { throw new NullPointerException("ID cannot be null"); }
//        validateMedicalHelpCategory(mhc);
//
//        // unique name and type
//        if (null != mhc.getName() && mhc.getName().trim().length() > 0) {
//            boolean nameOk = false;
//            try {
//                final MedicalHelpCategory found = mhcDao.findByNameAndType(
//                        mhc.getUnit().getId(), mhc.getName(), mhc.getType());
//                nameOk = (found == null || found.getId().equals(mhc.getId()));
//            } catch (IllegalStateException e) {
//                // expected state <- combination of name and type not found
//                nameOk = true;
//            }
//            if (nameOk) {
//                if (LOG.isLoggable(Level.FINER)) {
//                    LOG.finer("duplicite name check OK, name=" + mhc.getName());
//                }
//            } else {
//                ServerValidation.exception("duplicateValue", "name", (Object[]) null);
//            }
//        }
//
//        final MedicalHelpCategory mhcGae = mhcDao.getById(mhc.getId());
//        MedicalHelpCategory.mapFromDto(mhc, mhcGae);
//        mhcDao.persist(mhcGae);
//        LOG.info("category updated, id=" + mhc.getId()
//                + ", name=" + mhc.getName());
//    }

    /** {@inheritDoc} */
    @Transactional(readOnly = true)
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public List<Procedure> getProceduresByUnit(final Object unitId, final Event.Type type) {
        final List<Procedure> rslt;
        if (null != type) {
            rslt = procedureDao.findBy("unit", unitId, "type", type.toString(), "name");
        } else {
            rslt = procedureDao.findBy("unit", unitId, "name");
        }

        LOG.info("found proceduress, unitId=" + unitId + ", size=" + rslt.size());
        return rslt;
    }

//    /** {@inheritDoc} */
//    @Transactional(readOnly = true)
//    @Override
//    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
//    public MedicalHelpCategoryDto getMedicalHelpCategoryById(final Long mhcId) {
//        final MedicalHelpCategory mhcGae = mhcDao.getById(mhcId);
//        final Unit unitGae = unitDao.getById(mhcGae.getUnitId());
//        final MedicalHelpCategoryDto rslt = mhcGae.mapToDto();
//        rslt.setUnit(unitGae.mapToDto());
//        return rslt;
//    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public void deleteProcedure(final Object id) {
        procedureDao.remove(id);
        LOG.info("procedure deleted, id=" + id);
    }


    // -------------------------------------------------------- Assistant Stuff

    /**
     * Validation of a unit before persistence.
     * @param unit unit to be validated
     */
    private void validateUnit(final Unit unit) {
        if (null == unit) {
            throw new NullPointerException("unit cannot be null");
        }
        if (Utensils.stringIsBlank(unit.getName())) {
            throw new IllegalArgumentException("unit name cannot be blank");
        }
    }

    /**
     * Validation of a customer before persistence.
     * @param customer customer to be validated
     */
    private void validateCustomer(final Customer customer) {
        if (null == customer) {
            throw new NullPointerException("customer cannot be null");
        }
        if (null == customer.getUnit()) {
            throw new NullPointerException("unit cannot be null");
        }
        if (null == customer.getUnit().getId()) {
            throw new NullPointerException("unit ID cannot be null");
        }
        if (Utensils.stringIsBlank(customer.getFirstname())) {
            throw new IllegalArgumentException("first name cannot be blank");
        }
        if (Utensils.stringIsBlank(customer.getSurname())) {
            throw new IllegalArgumentException("surname cannot be blank");
        }

        // birth number
        if (Utensils.stringIsBlank(customer.getBirthNumber())) {
            LOG.warning("birth number is empty, fisrtname=" + customer.getFirstname()
                    + ", surname=" + customer.getSurname());
        } else {
            validationContainer.validate("birthNumber", "birthNumber", customer.getBirthNumber());
        }
        // phone number
        if (Utensils.stringIsBlank(customer.getPhoneNumber())) {
            LOG.warning("phone number is empty, fisrtname=" + customer.getFirstname()
                    + ", surname=" + customer.getSurname());
        } else {
            validationContainer.validate("phoneNumber", "phoneNumber", customer.getPhoneNumber());
        }
    }

    /**
     * Validation of a procedure before persistence.
     * @param proc procedure to be validated
     */
    private void validateProcedure(final Procedure proc) {
        if (null == proc) {
            throw new NullPointerException("procedure cannot be null");
        }
        if (null == proc.getUnit()) {
            throw new NullPointerException("unit cannot be null");
        }
        if (null == proc.getUnit().getId()) {
            throw new NullPointerException("unit ID cannot be null");
        }
        if (Utensils.stringIsBlank(proc.getName())) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (Utensils.stringIsBlank(proc.getMessageText())) {
            throw new IllegalArgumentException("message text cannot be blank");
        }
        if (proc.enumType() == Event.Type.IN_CALENDAR) {
            if (Utensils.stringIsBlank(proc.getColor())) {
                throw new NullPointerException("color cannot be blank");
            }
            if (6 != proc.getColor().length()) {
                throw new IllegalArgumentException("bad color format");
            }
            if (proc.getTime() <= 0) {
                throw new IllegalArgumentException("bad time");
            }
        }
    }

}
