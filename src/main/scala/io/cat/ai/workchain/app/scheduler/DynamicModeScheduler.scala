package io.cat.ai.workchain.app.scheduler

import io.cat.ai.workchain.core.scheduler.AbstractDagJobScheduler
import io.cat.ai.workchain.core.scheduler.manager.JobSetManagerMode

trait DynamicModeScheduler extends AbstractDagJobScheduler {

  def applyJobSetManagerMode(mode: JobSetManagerMode): AbstractDagJobScheduler
}