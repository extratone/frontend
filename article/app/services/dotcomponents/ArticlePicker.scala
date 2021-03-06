package services.dotcomponents

import model.ArticlePage
import experiments.{ActiveExperiments, Control, DotcomRendering, Excluded, Experiment, Participant}
import model.PageWithStoryPackage
import implicits.Requests._
import model.liveblog.{
  AudioBlockElement,
  BlockElement,
  CommentBlockElement,
  ContentAtomBlockElement,
  DocumentBlockElement,
  GuVideoBlockElement,
  ImageBlockElement,
  InstagramBlockElement,
  MapBlockElement,
  PullquoteBlockElement,
  RichLinkBlockElement,
  TableBlockElement,
  TextBlockElement,
  TweetBlockElement,
  VideoBlockElement,
}
import play.api.mvc.RequestHeader
import views.support.Commercial

object ArticlePageChecks {

  def isAdFree(page: PageWithStoryPackage, request: RequestHeader): Boolean = {
    page.item.content.shouldHideAdverts || Commercial.isAdFree(request)
  }

  def isDiscussionDisabled(page: PageWithStoryPackage): Boolean = {
    (!page.article.content.trail.isCommentable) && page.article.content.trail.isClosedForComments
  }

  def hasBlocks(page: PageWithStoryPackage): Boolean = {
    page.article.blocks match {
      case Some(b) => b.body.nonEmpty
      case None    => false
    }
  }

  def isSupportedType(page: PageWithStoryPackage): Boolean = {
    page match {
      case a: ArticlePage => true
      case _              => false
    }
  }

  def hasOnlySupportedElements(page: PageWithStoryPackage): Boolean = {
    // See: https://github.com/guardian/dotcom-rendering/blob/master/packages/frontend/web/components/lib/ArticleRenderer.tsx

    def unsupportedElement(blockElement: BlockElement) =
      blockElement match {
        case _: AudioBlockElement     => false
        case _: CommentBlockElement   => false
        case _: DocumentBlockElement  => false
        case _: GuVideoBlockElement   => false
        case _: ImageBlockElement     => false
        case _: InstagramBlockElement => false
        case _: MapBlockElement       => false
        case _: PullquoteBlockElement => false
        case _: RichLinkBlockElement  => false
        case _: TableBlockElement     => false
        case _: TextBlockElement      => false
        case _: TweetBlockElement     => false
        case _: VideoBlockElement     => false
        case ContentAtomBlockElement(_, atomtype) => {
          // ContentAtomBlockElement was expanded to include atomtype.
          // To support an atom type, just add it to supportedAtomTypes
          val supportedAtomTypes =
            List("audio", "chart", "explainer", "guide", "interactive", "profile", "qanda", "timeline")
          !supportedAtomTypes.contains(atomtype)
        }
        case _ => true
      }

    !page.article.blocks.exists(_.body.exists(_.elements.exists(unsupportedElement)))
  }

  def hasOnlySupportedMainElements(page: PageWithStoryPackage): Boolean = {
    // See: https://github.com/guardian/dotcom-rendering/blob/master/packages/frontend/web/components/lib/ArticleRenderer.tsx
    def unsupportedElement(blockElement: BlockElement) =
      blockElement match {
        case _: TextBlockElement  => false
        case _: ImageBlockElement => false
        case _                    => true
      }

    !page.article.blocks.exists(_.main.exists(_.elements.exists(unsupportedElement)))
  }

  // Custom Tag that can be added to articles + special reports tags while we don't support them
  private[this] val tagsBlockList: Set[String] = Set(
    "tracking/platformfunctional/dcrblacklist",
    "business/series/undercover-in-the-chicken-industry",
    "business/series/britains-debt-timebomb",
    "world/series/this-is-europe",
    "environment/series/the-polluters",
    "news/series/hsbc-files",
    "news/series/panama-papers",
    "us-news/homan-square",
    "uk-news/series/the-new-world-of-work",
    "world/series/the-new-arrivals",
    "news/series/nauru-files",
    "us-news/series/counted-us-police-killings",
    "australia-news/series/healthcare-in-detention",
    "society/series/this-is-the-nhs",
    "artanddesign/series/guardian-print-shop",
  )

  def isNotInTagBlockList(page: PageWithStoryPackage): Boolean = {
    !page.item.tags.tags.exists(t => tagsBlockList(t.id))
  }

  def isNotNumberedList(page: PageWithStoryPackage): Boolean = !page.item.isNumberedList

  def isNotPhotoEssay(page: PageWithStoryPackage): Boolean = !page.item.isPhotoEssay

  def isNotAGallery(page: PageWithStoryPackage): Boolean = !page.item.tags.isGallery

  def isNotAMP(request: RequestHeader): Boolean = !request.isAmp

  def isNotOpinion(page: PageWithStoryPackage): Boolean = !page.item.tags.isComment

  def isNotPaidContent(page: PageWithStoryPackage): Boolean = !page.article.tags.isPaidContent

}

object ArticlePicker {

  val logger = DotcomponentsLogger()

  private[this] def logRequest(msg: String, results: Map[String, String], page: PageWithStoryPackage)(implicit
      request: RequestHeader,
  ): Unit = {
    logger.withRequestHeaders(request).results(msg, results, page)
  }

  def primaryFeatures(page: PageWithStoryPackage, request: RequestHeader): Map[String, Boolean] = {
    Map(
      ("isSupportedType", ArticlePageChecks.isSupportedType(page)),
      ("hasBlocks", ArticlePageChecks.hasBlocks(page)),
      ("hasOnlySupportedElements", ArticlePageChecks.hasOnlySupportedElements(page)),
      ("hasOnlySupportedMainElements", ArticlePageChecks.hasOnlySupportedMainElements(page)),
      ("isNotPhotoEssay", ArticlePageChecks.isNotPhotoEssay(page)),
      ("isNotAGallery", ArticlePageChecks.isNotAGallery(page)),
      ("isNotAMP", ArticlePageChecks.isNotAMP(request)),
      ("isNotPaidContent", ArticlePageChecks.isNotPaidContent(page)),
      ("isNotInTagBlockList", ArticlePageChecks.isNotInTagBlockList(page)),
      ("isNotNumberedList", ArticlePageChecks.isNotNumberedList(page)),
    )
  }

  def forall(features: Map[String, Boolean]): Boolean = {
    features.forall({ case (_, isMet) => isMet })
  }

  def dcrArticle100PercentPage(page: PageWithStoryPackage, request: RequestHeader): Boolean = {
    val allowListFeatures = primaryFeatures(page, request)
    val article100PercentPageFeatures = allowListFeatures.filterKeys(
      Set(
        "isSupportedType",
        "isNotAGallery",
        "isNotAMP",
        "isNotInTagBlockList",
        "isNotPaidContent",
      ),
    )
    article100PercentPageFeatures.forall({ case (test, isMet) => isMet })
  }

  def dcrDisabled(request: RequestHeader): Boolean = {
    val dcrEnabled = conf.switches.Switches.DotcomRendering.isSwitchedOn
    val forceDCROff = request.forceDCROff
    !dcrEnabled || forceDCROff
  }

  def getTier(page: PageWithStoryPackage)(implicit request: RequestHeader): RenderType = {
    val primaryChecks = primaryFeatures(page, request)
    val hasPrimaryFeatures = forall(primaryChecks)

    val userInDCRGroup = ActiveExperiments.isParticipating(DotcomRendering)

    val tier =
      if (request.forceDCR) RemoteRender // dcrForced doesn't check the switch. This means that RemoteRender
      // is always going to be selected if `?dcr=true`, regardless of
      // the switch.
      else if (dcrDisabled(request)) LocalRenderArticle // dcrDisabled does check the switch.
      else if (userInDCRGroup && hasPrimaryFeatures) RemoteRender
      else LocalRenderArticle

    val isArticle100PercentPage = dcrArticle100PercentPage(page, request);
    val isAddFree = ArticlePageChecks.isAdFree(page, request);
    val pageTones = page.article.tags.tones.map(_.id).mkString(", ")

    def testGroup(experiment: Experiment): String =
      ActiveExperiments.groupFor(experiment) match {
        case Participant => "participant"
        case Control     => "control"
        case Excluded    => "excluded"
      }

    // include features that we wish to log but not allow-list against
    val features = primaryChecks.mapValues(_.toString) +
      ("dcrTestGroup" -> testGroup(DotcomRendering)) +
      ("userIsInCohort" -> userInDCRGroup.toString) +
      ("isAdFree" -> isAddFree.toString) +
      ("isArticle100PercentPage" -> isArticle100PercentPage.toString) +
      ("dcrCouldRender" -> hasPrimaryFeatures.toString) +
      ("pageTones" -> pageTones)

    if (tier == RemoteRender) {
      logRequest(s"path executing in dotcomponents", features, page)
    } else {
      logRequest(s"path executing in web", features, page)
    }

    tier
  }
}
