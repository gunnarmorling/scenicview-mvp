/**
 * Hibernate ScenicView, Great Views on your Data
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.scenicview.internal.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.hibernate.scenicview.spi.backend.DenormalizationBackend;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTask;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskHandler;
import org.hibernate.scenicview.spi.backend.model.DenormalizationTaskSequence;

/**
 * @author Gunnar Morling
 *
 */
public class DenormalizationQueue {

	private final DenormalizationBackend backend;
	private final List<Supplier<DenormalizationTask>> tasks = new ArrayList<>();

	public DenormalizationQueue(DenormalizationBackend backend) {
		this.backend = backend;
	}

	public void add(Supplier<DenormalizationTask> task) {
		this.tasks.add( task );
	}

	public void flush() {
		backend.process( new DenormalizationTaskSequenceImpl() );
	}

	private class DenormalizationTaskSequenceImpl implements DenormalizationTaskSequence {

		@Override
		public void forEach(DenormalizationTaskHandler handler) {
			for ( Supplier<DenormalizationTask> taskSource : tasks ) {
				taskSource.get().accept( handler );
			}
		}
	}
}
