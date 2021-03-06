/*
 * Copyright (c) Thomas Parker, 2009.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.cdom.facet;

import pcgen.base.solver.SolverFactory;
import pcgen.cdom.base.DataSetInitializedFacet;
import pcgen.cdom.enumeration.DataSetID;
import pcgen.cdom.facet.base.AbstractItemFacet;
import pcgen.rules.context.LoadContext;

/**
 * This stores the SolverFactory for each Data Load (DataSetID).
 */
public class SolverFactoryFacet extends
		AbstractItemFacet<DataSetID, SolverFactory> implements
		DataSetInitializedFacet
{

	private DataSetInitializationFacet datasetInitializationFacet;

	@Override
	public synchronized void initialize(LoadContext context)
	{
		DataSetID dsID = context.getDataSetID();
		set(dsID, context.getVariableContext().getFormulaSetup()
			.getSolverFactory());
	}

	public void init()
	{
		datasetInitializationFacet.addDataSetInitializedFacet(this);
	}

	public void setDataSetInitializationFacet(
		DataSetInitializationFacet datasetInitializationFacet)
	{
		this.datasetInitializationFacet = datasetInitializationFacet;
	}

}
