package services

import play.api.mvc.RequestHeader
import play.twirl.api.Html
import scala.util.matching.Regex

// ApplicationsSpecial2020Election is a temporary object introduced for the special handling
// of the US election Nov 2020 results election tracker, a ng-interactive page which we need an amp version of,
// something that DCR doesn't yet know how to do. It's essentially the hack version of
// ApplicationsDotcomRenderingInterface , which is not ready ; a (slower) work in progress.

object ApplicationsSpecial2020Election {

  val specialPathsToCapiIdsMap = Map(
    "/world/ng-interactive/2020/oct/20/covid-vaccine-tracker-when-will-a-coronavirus-vaccine-be-ready" -> "atom/interactive/interactives/2020/07/interactive-vaccine-tracker/amp-page",
    "/world/ng-interactive/2020/oct/29/covid-vaccine-tracker-when-will-a-coronavirus-vaccine-be-ready" -> "atom/interactive/interactives/2020/07/interactive-vaccine-tracker/amp-page",
    "/us-news/ng-interactive/2020/nov/03/us-election-2020-live-results-donald-trump-joe-biden-who-won-presidential-republican-democrat" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/03/us-election-2020-live-results-donald-trump-joe-biden-presidential-votes-arizona-nevada-pennsylvania-georgia" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/05/us-election-2020-live-results-donald-trump-joe-biden-presidential-votes-arizona-nevada-pennsylvania-georgia" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/07/us-election-2020-live-results-donald-trump-joe-biden-presidential-votes-pennsylvania-georgia-arizona-nevada" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/08/us-election-2020-live-results-donald-trump-joe-biden-presidential-votes-pennsylvania-georgia-arizona-nevada" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/12/us-election-results-2020-joe-biden-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/13/us-election-results-2020-joe-biden-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/14/us-election-results-2020-joe-biden-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/15/us-election-results-2020-joe-biden-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/16/us-election-results-2020-joe-biden-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    "/us-news/ng-interactive/2020/nov/17/us-election-results-2020-joe-biden-won-donald-trump-presidential-electoral-college-votes" -> "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
  )
  val specialPaths = specialPathsToCapiIdsMap.keys.toList

  val pathIsNovemberElectionTrackerRegex: Regex =
    """^/us-news/ng-interactive/2020/nov/\d\d/us-election-results-2020-""".r

  def pathIsElectionTracker(path: String): Boolean = {
    // This function was introduced to avoid having to update `specialPathsToCapiIdsMap` every day with new path
    // by allow listing anything of the form /us-news/ng-interactive/2020/nov/??/us-election-results-2020-*
    pathIsNovemberElectionTrackerRegex.findFirstIn(path).isDefined
  }

  def ensureStartingForwardSlash(str: String): String = {
    if (!str.startsWith("/")) ("/" + str) else str
  }

  def pathIsSpecialHanding(path: String): Boolean = {
    /*
      We pass the path through `ensureStartingForwardSlash` because
      when called from `ApplicationsDotcomRenderingInterface.getRenderingTier` it comes without starting slash, but
      when called from `InteractiveHtmlPage.html` it comes with it.
     */
    val path1 = ensureStartingForwardSlash(path)
    specialPaths.contains(path1) || pathIsElectionTracker(path1)
  }

  def defaultAtomIdToAmpAtomId(atomId: String): String = {
    /*
        This function transforms an atom id
          "interactives/2020/07/interactive-vaccine-tracker/default"
        into the corresponding amp capi query path
          "atom/interactive/interactives/2020/07/interactive-vaccine-tracker/amp-page"

        Election tracker:
          "interactives/2020/11/us-election/prod/default" (expected, to be confirmed)
          "atom/interactive/interactives/2020/11/us-election/prod/amp-page"
     */
    (Array("atom", "interactive") ++ atomId.split("/").dropRight(1) ++ Array("amp-page")).mkString("/")
  }

  def pathToAmpAtomId(path: String): String = {
    /*
        This version is a more limited, but much more robust version, of `defaultAtomIdToAmpAtomId`
        In particular, it doesn't rely on a particular format for the atom ids, and instead
        maps paths directly to capi query ids, which is fine since we essentially only want to support few urls.

        Update, 17th Nov: with the introduction of `pathIsNovemberElectionTrackerRegex` and `pathIsElectionTracker`
        The paths that are not in `specialPathsToCapiIdsMap`, but pass the `pathIsElectionTracker` test are missing
        a CAPI Id. When that happens we are going to default to the election tracker atom Id.
     */
    specialPathsToCapiIdsMap.getOrElse(
      ensureStartingForwardSlash(path),
      "atom/interactive/interactives/2020/11/us-election/prod/amp-page",
    )
  }

  def ampTagHtml(path: String)(implicit request: RequestHeader): Html = {
    if (ApplicationsSpecial2020Election.pathIsSpecialHanding(path)) {
      Html(
        s"""<link rel="amphtml" href="https://amp.theguardian.com${ensureStartingForwardSlash(path)}">""",
      )
    } else {
      Html("")
    }
  }
}
