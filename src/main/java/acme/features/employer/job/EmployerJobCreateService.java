/*
 * EmployerJobCreateService.java
 *
 * Copyright (C) 2012-2025 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.features.employer.job;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import acme.client.components.models.Dataset;
import acme.client.components.views.SelectChoices;
import acme.client.services.AbstractGuiService;
import acme.client.services.GuiService;
import acme.entities.companies.Company;
import acme.entities.jobs.Job;
import acme.realms.Employer;

@GuiService
public class EmployerJobCreateService extends AbstractGuiService<Employer, Job> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private EmployerJobRepository repository;

	// AbstractGuiService interface -------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Job object;
		Employer employer;

		employer = (Employer) super.getRequest().getPrincipal().getActiveRealm();
		object = new Job();
		object.setDraftMode(true);
		object.setEmployer(employer);

		super.getBuffer().addData(object);
	}

	@Override
	public void bind(final Job object) {
		assert object != null;

		int contractorId;
		Company contractor;

		contractorId = super.getRequest().getData("contractor", int.class);
		contractor = this.repository.findContractorById(contractorId);

		super.bindObject(object, "ticker", "title", "deadline", "salary", "score", "moreInfo", "description");
		object.setContractor(contractor);
	}

	@Override
	public void validate(final Job object) {
		assert object != null;
	}

	@Override
	public void perform(final Job object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final Job object) {
		assert object != null;

		int employerId;
		Collection<Company> contractors;
		SelectChoices choices;
		Dataset dataset;

		employerId = super.getRequest().getPrincipal().getActiveRealm().getId();
		contractors = this.repository.findContractorsByEmployerId(employerId);
		choices = SelectChoices.from(contractors, "name", object.getContractor());

		dataset = super.unbindObject(object, "ticker", "title", "deadline", "salary", "score", "moreInfo", "description", "draftMode");
		dataset.put("contractor", choices.getSelected().getKey());
		dataset.put("contractors", choices);

		super.getResponse().addData(dataset);
	}

}
