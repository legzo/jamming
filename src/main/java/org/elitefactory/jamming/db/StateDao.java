package org.elitefactory.jamming.db;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class StateDao {

	@PersistenceContext
	private EntityManager em;

	@Transactional
	public void save(State state) {
		em.persist(state);
	}

	@SuppressWarnings("unchecked")
	public List<State> find(Date from, Date to) {
		return em.createQuery("select state from State state where state.time > :from AND state.time < :to ")
				.setParameter("from", from, TemporalType.TIMESTAMP).setParameter("to", to, TemporalType.TIMESTAMP)
				.getResultList();

	}
}
