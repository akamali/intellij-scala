package org.jetbrains.plugins.scala.codeInspection

import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import org.jetbrains.plugins.scala.base.SimpleTestCase

class GeneralInspectionSanityTest extends SimpleTestCase {

  def acquireAllInspectionEPs(): Seq[LocalInspectionEP] =
    LocalInspectionEP.LOCAL_INSPECTION
      .getExtensions()
      .toSeq

  def acquireAllScalaInspectionEPs(): Seq[LocalInspectionEP] =
    acquireAllInspectionEPs().filter(ep => ep.language == "Scala" || Option(ep.groupPath).exists(_.toLowerCase.contains("scala")))

  def getDescription(inspectionEP: LocalInspectionEP): String = {
    val description = new LocalInspectionToolWrapper(inspectionEP).loadDescription()
    assert(description != null, s"The description for the inspection ${inspectionEP.getShortName} is null")
    description
  }

  def test_no_lowercase_language_used(): Unit = {
    assert(!acquireAllInspectionEPs()
      .flatMap(insp => Option(insp.language))
      .exists(lang => lang != "Scala" && lang.toLowerCase == "scala"))
  }

  def test_all_inspections_have_descriptions(): Unit = {
    val inspectionsWithoutProperDescription =
      acquireAllScalaInspectionEPs().filter { inspectionEP =>
        val description = getDescription(inspectionEP)
        description == null ||
          description.length <= 5
      }.sortBy(_.getShortName)
        .map(insp => s"${insp.getShortName} (${insp.getDisplayName})")

    assert(inspectionsWithoutProperDescription.isEmpty,
      s"The following inspection do not have a description file:\n  ${inspectionsWithoutProperDescription.mkString(",\n  ")}")
  }

  def test_all_shortNames_are_unique(): Unit = {
    val allShortNames = acquireAllInspectionEPs().map(_.getShortName).groupBy(identity).view.mapValues(_.length)
    val scalaShortNames = acquireAllScalaInspectionEPs().map(_.getShortName)

    scalaShortNames.foreach { scalaShortName =>
      assert(allShortNames(scalaShortName) == 1, s"shortName $scalaShortName exists multiple times!")
    }
  }

  def test_all_inspection_descriptions_have_tooltip_end(): Unit = {
    val inspectionsWithoutProperDescription =
      acquireAllScalaInspectionEPs().filterNot { inspectionEP =>
        getDescription(inspectionEP).contains("<!-- tooltip end -->")
      }.sortBy(_.getShortName)
        .map(insp => s"${insp.getShortName} (${insp.getDisplayName})")

    assert(inspectionsWithoutProperDescription.isEmpty,
      s"The following inspection's description files don't have <!-- tooltip end -->:\n  ${inspectionsWithoutProperDescription.mkString(",\n  ")}")
  }

  def test_all_inspection_code_blocks_are_indented_by_at_least_two_space(): Unit = {
    val regex = raw"""<pre><code>((.|\n)*)</pre></code>""".r
    val inspectionsWithoutProperDescription =
      acquireAllScalaInspectionEPs().filterNot { inspectionEP =>
        regex.findAllMatchIn(getDescription(inspectionEP)).forall { aMatch =>
          val code = aMatch.group(1)
          code.linesIterator.forall(s => s.isBlank || s.startsWith("  "))
        }
      }.sortBy(_.getShortName)
        .map(insp => s"${insp.getShortName} (${insp.getDisplayName})")

    assert(inspectionsWithoutProperDescription.isEmpty,
      s"The following inspection's description files have code blocks with wrong indentation:\n  ${inspectionsWithoutProperDescription.mkString(",\n  ")}")
  }

  /*
  def test_inspection_list_is_correct(): Unit = {
    val inspectionEPs = acquireAllScalaInspectionEPs()

    assert(expectedInspections.sorted == expectedInspections, "Please keep expectedInspections sorted!")
    val doubleExpectedInspections =
      expectedInspections
        .groupBy(identity)
        .mapValues(_.length)
        .filter(_._2 > 1)
        .keys
        .toSeq
    assert(doubleExpectedInspections.isEmpty,
      s"The following expectedInspections are duplicated: ${doubleExpectedInspections.mkString(", ")}")

    val expectedInspectionSet = expectedInspections.toSet
    // check if all expected inspections are present
    val foundInspections = inspectionEPs.map(_.implementationClass.split('.').last).toSet

    val nonExistentInspections = expectedInspectionSet -- foundInspections
    val unregisteredInspections = foundInspections -- expectedInspectionSet

    assert(nonExistentInspections.isEmpty,
      s"The following expected inspections were not found: ${nonExistentInspections.mkString(", ")}")

    assert(unregisteredInspections.isEmpty,
      s"Please add the following inspections to expectedInspections: ${unregisteredInspections.mkString(", ")}")
  }
  
  val expectedInspections = Seq(
    "AbstractValueInTraitInspection",
    "AccessorLikeMethodInspection$EmptyParentheses",
    "AccessorLikeMethodInspection$UnitReturnType",
    "AmmoniteUnresolvedLibrary",
    "AnnotatorBasedErrorInspection",
    "ApparentResultTypeRefinementInspection",
    "AppliedTypeLambdaCanBeSimplifiedInspection",
    "AutoTuplingInspection",
    "BuiltinMatcherExistsInspection",
    "BundledCompoundInspection",
    "CaseClassParamInspection",
    "ChainedPackageInspection",
    "CollectHeadOptionInspection",
    "ComparingDiffCollectionKindsInspection",
    "ComparingLengthInspection",
    "ComparingUnrelatedTypesInspection",
    "ConcealedApplyCall",
    "ConvertExpressionToSAMInspection",
    "ConvertibleToMethodValueInspection",
    "CorrespondsUnsortedInspection",
    "DangerousCatchAllInspection",
    "DeprecatedViewBoundInspection",
    "DoubleNegationInspection",
    "DropTakeToSliceInspection",
    "EmptyCheckInspection",
    "EmptyParenOverrideInspection$JavaAccessorMethodOverriddenAsEmptyParenInspection",
    "EmptyParenOverrideInspection$ParameterlessMemberOverriddenAsEmptyParenInspection",
    "EmulateFlattenInspection",
    "EqualityToSameElementsInspection",
    "ExistsEqualsInspection",
    "ExistsForallReplaceInspection",
    "FieldFromDelayedInitInspection",
    "FilterEmptyCheckInspection",
    "FilterHeadOptionInspection",
    "FilterOtherContainsInspection",
    "FilterSetContainsInspection",
    "FilterSizeInspection",
    "FindAndMapToApplyInspection",
    "FindEmptyCheckInspection",
    "FloatLiteralEndingWithDecimalPointInspection",
    "FoldTrueAndInspection",
    "ForwardReferenceInspection",
    "FunctionTupleSyntacticSugarInspection",
    "GetGetOrElseInspection",
    "GetOrElseNullInspection",
    "HashCodeUsesVarInspection",
    "HeadOrLastOptionInspection",
    "IfElseToFilteredOptionInspection",
    "IfElseToOptionInspection",
    "IndexBoundsCheckInspection",
    "JavaAccessorEmptyParenCallInspection",
    "KindProjectorSimplifyTypeProjectionInspection",
    "KindProjectorUseCorrectLambdaKeywordInspection",
    "LanguageFeatureInspection",
    "LastIndexToLastInspection",
    "LegacyStringFormattingInspection",
    "LoopVariableNotUpdatedInspection",
    "MapFlattenInspection",
    "MapGetGetInspection",
    "MapGetOrElseBooleanInspection",
    "MapGetOrElseInspection",
    "MapKeysInspection",
    "MapLiftInspection",
    "MapToBooleanContainsInspection",
    "MapValuesInspection",
    "MatchToPartialFunctionInspection",
    "MultipleArgLists",
    "NameBooleanParametersInspection",
    "NestedStatefulMonadsInspection",
    "NoTailRecursionAnnotationInspection",
    "NotImplementedCodeInspection",
    "OptionEqualsSomeToContainsInspection",
    "ParameterlessAccessInspection$EmptyParenMethod",
    "ParameterlessAccessInspection$JavaMutator",
    "ParameterlessOverrideInspection$EmptyParenMethod",
    "ParameterlessOverrideInspection$JavaMutator",
    "ParameterlessOverrideInspection$MutatorLikeMethod",
    "PostfixMethodCallInspection",
    "RangeToIndicesInspection",
    "RedundantBlockInspection",
    "RedundantCollectionConversionInspection",
    "RedundantDefaultArgumentInspection",
    "RedundantHeadOrLastOptionInspection",
    "RedundantNewCaseClassInspection",
    "ReferenceMustBePrefixedInspection",
    "RelativeImportInspection",
    "RemoveRedundantReturnInspection",
    "ReplaceToWithUntilInspection",
    "ReverseIteratorInspection",
    "ReverseMapInspection",
    "ReverseTakeReverseInspection",
    "SameElementsToEqualsInspection",
    "SbtReplaceProjectWithProjectInInspection",
    "ScalaDefaultFileTemplateUsageInspection",
    "ScalaDeprecatedIdentifierInspection",
    "ScalaDeprecationInspection",
    "ScalaDocInlinedTagInspection",
    "ScalaDocMissingParameterDescriptionInspection",
    "ScalaDocParserErrorInspection",
    "ScalaDocUnbalancedHeaderInspection",
    "ScalaDocUnclosedTagWithoutParserInspection",
    "ScalaDocUnknownParameterInspection",
    "ScalaDocUnknownTagInspection",
    "ScalaFileNameInspection",
    "ScalaMalformedFormatStringInspection",
    "ScalaPackageNameInspection",
    "ScalaRedundantCastInspection",
    "ScalaRedundantConversionInspection",
    "ScalaUnnecessaryParenthesesInspection",
    "ScalaUnnecessarySemicolonInspection",
    "ScalaUnreachableCodeInspection",
    "ScalaUnusedExpressionInspection",
    "ScalaUnusedSymbolInspection",
    "ScalaXmlUnmatchedTagInspection",
    "ScalastyleCodeInspection",
    "SideEffectsInMonadicTransformationInspection",
    "SimplifiableFoldOrReduceInspection",
    "SimplifyBooleanInspection",
    "SimplifyBooleanMatchInspection",
    "SingleImportInspection",
    "SizeToLengthInspection",
    "SomeToOptionInspection",
    "SortFilterInspection",
    "SortedMaxMinInspection",
    "SourceNotClosedInspection",
    "ToSetAndBackInspection",
    "TypeAnnotationInspection",
    "TypeCheckCanBeMatchInspection",
    "TypeParameterShadowInspection",
    "UnitInMapInspection",
    "UnitMethodInspection$ExplicitAssignment",
    "UnitMethodInspection$ExplicitType",
    "UnitMethodInspection$FunctionDefinition",
    "UnitMethodInspection$Parameterless",
    "UnitMethodInspection$ProcedureDeclaration",
    "UnitMethodInspection$ProcedureDefinition",
    "UnnecessaryPartialFunctionInspection",
    "UnzipSingleElementInspection",
    "VarCouldBeValInspection",
    "VariableNullInitializerInspection",
    "VariablePatternShadowInspection",
    "ZeroIndexToHeadInspection",
    "ZipWithIndexInspection",
  )

   */
}
