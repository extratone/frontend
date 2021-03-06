package experiments

import conf.switches.{Owner, SwitchGroup}
import experiments.ParticipationGroups._
import org.joda.time.LocalDate
import conf.switches.Owner.group
import conf.switches.SwitchGroup.Commercial

object ActiveExperiments extends ExperimentsDefinition {
  override val allExperiments: Set[Experiment] = Set(
    DotcomRendering,
    NGInteractiveDCR,
    UseAusCmp,
  )

  implicit val canCheckExperiment = new CanCheckExperiment(this)
}

object DotcomRendering
    extends Experiment(
      name = "dotcom-rendering",
      description = "Show DCR pages to users including those with comments",
      owners = Seq(Owner.withGithub("shtukas")),
      sellByDate = new LocalDate(2020, 12, 1),
      participationGroup = Perc50, // Also see ArticlePicker.scala - our main filter mechanism is by page features
    )

object NGInteractiveDCR
    extends Experiment(
      name = "ng-interactive-dcr",
      description = "Use DCR to render (ng)-interactives",
      owners = Seq(Owner.withGithub("shtukas")),
      sellByDate = new LocalDate(2021, 6, 1),
      participationGroup = Perc0B,
    )

object NewsletterEmbedDesign
    extends Experiment(
      name = "new-newsletter-embed-designs",
      description = "New newsletter signup embeds for discoverability OKR",
      owners = Seq(Owner.withGithub("buck06191")),
      sellByDate = new LocalDate(2020, 11, 30),
      participationGroup = Perc20A,
    )

object UseAusCmp
    extends Experiment(
      name = "use-aus-cmp",
      description = "Use AU framework in CMP in Australia",
      owners = group(Commercial),
      sellByDate = new LocalDate(2020, 11, 27),
      participationGroup = Perc0D,
    )
