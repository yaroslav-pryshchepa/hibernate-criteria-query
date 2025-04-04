package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error occurred while inserting phone "
                    + "into the database: " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return phone;
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> criteriaQuery = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> root = criteriaQuery.from(Phone.class);
            Predicate totalPredicate = criteriaBuilder.isNotNull(root.get("id"));
            CriteriaBuilder.In<String> paramPredicate;
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                paramPredicate = criteriaBuilder.in(root.get(entry.getKey()));
                for (String value : entry.getValue()) {
                    paramPredicate.value(value);
                }
                totalPredicate = criteriaBuilder.and(totalPredicate == null
                        ? paramPredicate : totalPredicate, paramPredicate);
            }
            criteriaQuery.where(totalPredicate);
            return session.createQuery(criteriaQuery).list();
        } catch (Exception e) {
            throw new RuntimeException("Can't execute query from findAll() method for parameters: "
                    + params, e);
        }
    }
}
