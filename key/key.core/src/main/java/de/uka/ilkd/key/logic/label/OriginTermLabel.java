package de.uka.ilkd.key.logic.label;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.key_project.util.collection.ImmutableArray;

import de.uka.ilkd.key.java.JavaInfo;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.TypeConverter;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.PosInTerm;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.SequentChangeInfo;
import de.uka.ilkd.key.logic.SequentFormula;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.TermFactory;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.logic.op.ProgramVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.proof.mgt.SpecificationRepository;
import de.uka.ilkd.key.rule.label.OriginTermLabelRefactoring;

/**
 * <p> An {@link OriginTermLabel} saves a term's origin in the JML specification
 * ({@link #getOrigin()}) as well as the origins of all of its subterms and former
 * subterms ({@link #getSubtermOrigins()}). </p>
 *
 * <p> For this to work correctly, you must call
 * {@link #collectSubtermOrigins(Term, TermBuilder)} for every top-level formula in your
 * original proof obligation. </p>
 *
 * <p> Before doing this, you can call {@link TermBuilder#addLabelToAllSubs(Term, TermLabel)}
 * for every term you have added to the original contract in your PO to add an
 * {@link OriginTermLabel}
 * of your choosing. Terms for which you do not do this get a label of the form
 * {@code new OriginTermLabel(SpecType.NONE, null, -1)}. </p>
 *
 * @author lanzinger
 */
public class OriginTermLabel implements TermLabel {

    /**
     * Display name for {@link OriginTermLabel}s.
     */
    public final static Name NAME = new Name("origin");

    /**
     * @see #getChildCount()
     */
    public final static int CHILD_COUNT = 2;

    /**
     * The term's origin.
     * @see #getOrigin()
     */
    private Origin origin;

    /**
     * The origins of the term's sub-terms and former sub-terms.
     * @see #getSubtermOrigins()
     */
    private Set<Origin> subtermOrigins;

    /**
     * Creates a new {@link OriginTermLabel}.
     *
     * @param origin the term's origin.
     */
    public OriginTermLabel(Origin origin) {
        this.origin = origin;
        this.subtermOrigins = new HashSet<>();
    }

    /**
     * Creates a new {@link OriginTermLabel}.
     *
     * @param origin the term's origin.
     * @param subtermOrigins the origins of the term's (former) subterms.
     */
    public OriginTermLabel(Origin origin, Set<Origin> subtermOrigins) {
        this(origin);
        this.subtermOrigins.addAll(subtermOrigins);
        this.subtermOrigins = this.subtermOrigins.stream()
                .filter(o -> o.specType != SpecType.NONE).collect(Collectors.toSet());
    }

    /**
     * Creates a new {@link OriginTermLabel}.
     *
     * @param specType the JML spec type the term originates from.
     * @param file the file the term originates from.
     * @param line the line in the file.
     * @param subtermOrigins the origins of the term's (former) subterms.
     */
    public OriginTermLabel(SpecType specType, String file, int line, Set<Origin> subtermOrigins) {
        this(specType, file, line);
        this.subtermOrigins.addAll(subtermOrigins);
        this.subtermOrigins = this.subtermOrigins.stream()
                .filter(o -> o.specType != SpecType.NONE).collect(Collectors.toSet());
    }

    /**
     * Creates a new {@link OriginTermLabel}.
     *
     * @param specType the JML spec type the term originates from.
     * @param file the file the term originates from.
     * @param line the line in the file.
     */
    public OriginTermLabel(SpecType specType, String file, int line) {
        String filename = file == null || file.equals("no file")
                ? null
                : new File(file).getName();
                //weigl: fix #1504: On Windows the filename was not parseable as it was not a valid filename (containing ':').
                // use more liberal old File API instead of Paths.get(file).getFileName().toString();

        this.origin = new Origin(specType, filename, line);
        this.subtermOrigins = new HashSet<>();
    }

    /**
     * Creates a new {@link OriginTermLabel}.
     *
     * @param subtermOrigins the origins of the term's (former) subterms.
     */
    public OriginTermLabel(Set<Origin> subtermOrigins) {
        this.origin = new Origin(SpecType.NONE, null, -1);
        this.subtermOrigins = new HashSet<>();
        this.subtermOrigins.addAll(subtermOrigins);
        this.subtermOrigins = this.subtermOrigins.stream()
                .filter(o -> o.specType != SpecType.NONE).collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((subtermOrigins == null) ? 0 : subtermOrigins.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OriginTermLabel) {
            OriginTermLabel other = (OriginTermLabel) obj;
            return other.origin.equals(origin) && other.subtermOrigins.equals(subtermOrigins);
        } else {
            return false;
        }
    }

    /**
     * <p> Determines whether an {@code OriginTermLabel} can be added to the specified term. </p>
     *
     * <p> E.g., no labels should be added to terms whose operator is a heap variable as this leads
     * to various problems during proof search. </p>
     *
     * @param term a term
     * @param services services.
     * @return {@code true} iff an {@code OriginTermLabel} can be added to the specified term.
     */
    public static boolean canAddLabel(Term term, Services services) {
        return canAddLabel(term.op(), services);
    }

    /**
     * <p> Determines whether an {@code OriginTermLabel} can be added to a term with the specified
     * operator. </p>
     *
     * <p> E.g., no labels should be added to terms whose operator is a heap variable as this leads
     * to various problems during proof search. </p>
     *
     * @param op the specified operator.
     * @param services services.
     * @return {@code true} iff an {@code OriginTermLabel} can be added to a term
     *  with the specified operator.
     */
    public static boolean canAddLabel(Operator op, Services services) {
        final TypeConverter tc = services.getTypeConverter();
        final JavaInfo ji = services.getJavaInfo();

        if (op.arity() == 0) {
            Sort sort = op.sort(new ImmutableArray<>());

            if (sort.extendsTrans(Sort.FORMULA)) {
                return true;
            } else if (op instanceof ProgramVariable) {
                return !sort.extendsTrans(tc.getHeapLDT().targetSort())
                        && !sort.extendsTrans(tc.getLocSetLDT().targetSort())
                        && !op.name().equals(ji.getInv().name())
                        && !op.name().toString().endsWith(SpecificationRepository.LIMIT_SUFFIX);
            } else {
                return false;
            }
        } else {
            return !(op instanceof Function);
        }
    }

    /**
     * Removes all {@link OriginTermLabel} from the specified sequent.
     *
     * @param seq the sequent to transform.
     * @param services services.
     * @return the resulting sequent change info.
     */
    public static SequentChangeInfo removeOriginLabels(Sequent seq, Services services) {
        SequentChangeInfo changes = null;

        for (int i = 1; i <= seq.size(); ++i) {
            SequentFormula oldFormula = seq.getFormulabyNr(i);
            SequentFormula newFormula = new SequentFormula(
                    OriginTermLabel.removeOriginLabels(oldFormula.formula(), services));
            SequentChangeInfo change = seq.changeFormula(
                    newFormula,
                    PosInOccurrence.findInSequent(seq, i, PosInTerm.getTopLevel()));

            if (changes == null) {
                changes = change;
            } else {
                changes.combine(change);
            }
        }

        return changes;
    }

    /**
     * Removes all {@link OriginTermLabel} from the specified term and its sub-terms.
     *
     * @param term the term to transform.
     * @param services services.
     * @return the transformed term.
     */
    public static Term removeOriginLabels(Term term, Services services) {
        if (term == null) {
            return null;
        }

        List<TermLabel> labels = term.getLabels().toList();
        final TermLabel originTermLabel = term.getLabel(NAME);
        final TermFactory tf = services.getTermFactory();
        final ImmutableArray<Term> oldSubs = term.subs();
        Term[] newSubs = new Term[oldSubs.size()];

        if (originTermLabel != null) {
            labels.remove(originTermLabel);
        }

        for (int i = 0; i < newSubs.length; ++i) {
            newSubs[i] = removeOriginLabels(oldSubs.get(i), services);
        }

        return tf.createTerm(term.op(),
                             newSubs,
                             term.boundVars(),
                             term.javaBlock(),
                             new ImmutableArray<>(labels));
    }


    /**
     * Compute the common origin from all origins in the passed origins set.
     * @param origins the passed origins set
     * @return the computed common origin
     */
    public static Origin computeCommonOrigin(final Set<Origin> origins) {
        SpecType commonSpecType = null;
        String commonFileName = null;
        int commonLine = -1;

        for (Origin origin : origins) {
            if (commonSpecType == null) {
                commonSpecType = origin.specType;
            } else if (commonSpecType != origin.specType) {
                commonSpecType = SpecType.NONE;
                commonFileName = null;
                commonLine = -1;
                break;
            }

            if (commonFileName == null) {
                commonFileName = origin.fileName;
            } else if (!commonFileName.equals(origin.fileName)) {
                commonFileName = Origin.MULTIPLE_FILES;
                commonLine = Origin.MULTIPLE_LINES;
            }

            if (commonLine == -1) {
                commonLine = origin.line;
            } else if (commonLine != origin.line) {
                commonLine = Origin.MULTIPLE_LINES;
            }
        }

        if (commonSpecType == null) {
            commonSpecType = SpecType.NONE;
        }

        return new Origin(commonSpecType, commonFileName, commonLine);
    }

    /**
     * This method transforms a term in such a way that
     * every {@link OriginTermLabel} contains all of the correct
     * {@link #getSubtermOrigins()}.
     *
     * @param term the term to transform.
     * @param services services.
     * @return the transformed term.
     */
    public static Term collectSubtermOrigins(Term term, Services services) {
        if (!canAddLabel(term, services)) {
            return term;
        }

        SubTermOriginData newSubs = getSubTermOriginData(term.subs(), services);
        final ImmutableArray<TermLabel> labels =
                computeOriginLabelsFromSubTermOrigins(term, newSubs.origins);

        return services.getTermFactory().createTerm(term.op(),
                                                    newSubs.terms,
                                                    term.boundVars(),
                                                    term.javaBlock(),
                                                    labels);
    }

    @Override
    public String toString() {
        return "" + NAME + "(" + origin + ") (" + subtermOrigins + ")";
    }

    @Override
    public Name name() {
        return NAME;
    }

    @Override
    public Object getChild(int i) {
        if (i == 0) {
            return origin;
        } else if (i == 1) {
            return subtermOrigins;
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount() {
        return CHILD_COUNT;
    }

    @Override
    public boolean isProofRelevant() {
        return false;
    }

    /**
     *
     * @return the term's origin.
     */
    public Origin getOrigin() {
        return origin;
    }

    /**
     * <p> Returns the origins of the term's sub-terms and former sub-terms. </p>
     *
     * <p> Note that you need to have called {@link #collectSubtermOrigins(Term, TermBuilder)}
     * for this method to work correctly. </p>
     *
     * @return the origins of the term's sub-terms and former sub-terms.
     * @see OriginTermLabelRefactoring
     */
    public Set<Origin> getSubtermOrigins() {
        return Collections.unmodifiableSet(subtermOrigins);
    }


    private static ImmutableArray<TermLabel>
                            computeOriginLabelsFromSubTermOrigins(final Term term,
                                                                  final Set<Origin> origins) {
        List<TermLabel> labels = term.getLabels().toList();
        final OriginTermLabel oldLabel = (OriginTermLabel) term.getLabel(NAME);

        if (oldLabel != null) {
            labels.remove(oldLabel);

            if ((!origins.isEmpty() || oldLabel.getOrigin().specType != SpecType.NONE)) {
                labels.add(new OriginTermLabel(
                        oldLabel.getOrigin().specType,
                        oldLabel.getOrigin().fileName,
                        oldLabel.getOrigin().line,
                        origins));
            }
        } else if (!origins.isEmpty()) {
            final OriginTermLabel newLabel =
                    new OriginTermLabel(computeCommonOrigin(origins), origins);

            labels.add(newLabel);
        }
        return new ImmutableArray<>(labels);
    }

    /**
     * @param subs the sub-terms to be searched
     * @param services a services object used for getting type information
     *                 and creating the new sub-term
     * @return origin information about the searched sub-terms stored in a
     *                {@link SubTermOriginData} object.
     */
    private static SubTermOriginData getSubTermOriginData(final ImmutableArray<Term> subs,
                                                          final Services services) {
        Term[] newSubs = new Term[subs.size()];
        Set<Origin> origins = new HashSet<>();

        for (int i = 0; i < newSubs.length; ++i) {
            newSubs[i] = collectSubtermOrigins(subs.get(i), services);
            final OriginTermLabel subLabel = (OriginTermLabel) newSubs[i].getLabel(NAME);

            if (subLabel != null) {
                origins.add(subLabel.getOrigin());
                origins.addAll(subLabel.getSubtermOrigins());
            }
        }
        return new SubTermOriginData(newSubs, origins);
    }

    /**
     * This class stores an array of sub-terms and a set of all their origins.
     * It is used when recursively collecting all origins from a term's sub-terms
     * for setting its respective origin labels. The information of the sub-terms
     * are used for propagating their origin label information upwards to their
     * enclosing term.
     *
     * @author Michael Kirsten
     *
     */
    private static class SubTermOriginData {
        /**  All collected sub-terms */
        public final Term[] terms;
        /** All collected origins */
        public final Set<Origin> origins;

        /**
         * This method constructs an object of type {@link SubTermOriginData}.
         * @param subterms the collected sub-terms
         * @param subtermOrigins the origin information collected from these sub-terms
         */
        public SubTermOriginData(Term[] subterms,
                                 Set<Origin> subtermOrigins) {
            this.terms = subterms;
            this.origins = subtermOrigins;
        }
    }

    /**
     * An origin encapsulates some information about where in the JML specification a term
     * originates from.
     *
     * @author lanzinger
     */
    public static class Origin implements Comparable<Origin> {

        /**
         * Placeholder file name used for implicit specifications.
         */
        public static final String IMPLICIT_FILE_NAME = "<implicit>";

        /**
         * Placeholder line number used for implicit specifications.
         */
        public static final int IMPLICIT_LINE = -1;

        /**
         * Placeholder line number used for specifications across multiple lines.
         */
        public static final String MULTIPLE_FILES = "<multiple>";

        /**
         * Placeholder line number used for specifications across multiple lines.
         */
        public static final int MULTIPLE_LINES = -2;

        /**
         * The JML spec type the term originates from.
         */
        public final SpecType specType;

        /**
         * The file the term originates from.
         */
        public final String fileName;

        /**
         * The line in the file the term originates from.
         */
        public final int line;

        /**
         * Creates a new {@link OriginTermLabel.Origin}.
         *
         * @param specType the JML spec type the term originates from.
         * @param fileName the file the term originates from.
         * @param line the line in the file.
         */
        public Origin(SpecType specType, String fileName, int line) {
            this.specType = specType;
            this.fileName = fileName == null ? IMPLICIT_FILE_NAME : fileName;
            this.line = line;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(specType.toString());

            if (fileName.equals(IMPLICIT_FILE_NAME)) {
                sb.append(" (implicit)");
            } else if (fileName.equals(MULTIPLE_FILES)) {
                sb.append(" (multiple files)");
            } else {
                sb.append(" @ ");
                sb.append(fileName);

                if (line == MULTIPLE_LINES) {
                    sb.append(" (multiple lines)");
                } else {
                    sb.append(" @ line ");
                    sb.append(line);
                }
            }

            return sb.toString();
        }

        @Override
        public int compareTo(Origin other) {
            int result = specType.toString().compareTo(other.specType.toString());

            if (result == 0) {
                result = fileName.compareTo(other.fileName);

                if (result == 0) {
                    result = Integer.compare(line, other.line);
                }
            }

            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
            result = prime * result + line;
            result = prime * result + ((specType == null) ? 0 : specType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Origin other = (Origin) obj;

            if (fileName == null) {
                if (other.fileName != null) {
                    return false;
                }
            } else if (!fileName.equals(other.fileName)) {
                return false;
            }

            if (line != other.line) {
                return false;
            }

            if (specType != other.specType) {
                return false;
            }

            return true;
        }
    }

    /**
     * A {@code SpecType} is any type of JML specification which gets translated into JavaDL.
     *
     * @author lanzinger
     * @see OriginTermLabel.Origin
     */
    public static enum SpecType {

        /**
         * accessible
         */
        ACCESSIBLE("accessible"),

        /**
         * assignable
         */
        ASSIGNABLE("assignable"),

        /**
         * decreases
         */
        DECREASES("decreases"),

        /**
         * measured_by
         */
        MEASURED_BY("measured_by"),

        /**
         * invariant
         */
        INVARIANT("invariant"),

        /**
         * loop_invariant
         */
        LOOP_INVARIANT("loop_invariant"),

        /**
         * loop_invariant_free
         */
        LOOP_INVARIANT_FREE("loop_invariant_free"),

        /**
         * requires
         */
        REQUIRES("requires"),

        /**
         * requires_free
         */
        REQUIRES_FREE("requires_free"),

        /**
         * ensures
         */
        ENSURES("ensures"),

        /**
         * ensures_free
         */
        ENSURES_FREE("ensures_free"),

        /**
         * signals
         */
        SIGNALS("signals"),

        /**
         * signals_only
         */
        SIGNALS_ONLY("signals_only"),

        /**
         * breaks
         */
        BREAKS("breaks"),

        /**
         * continues
         */
        CONTINUES("continues"),

        /**
         * returns
         */
        RETURNS("returns"),

        /**
         * None. Used for terms that do not originate from a JML spec and terms whose origin was
         * not set upon their creation.
         */
        NONE("<none>");

        /**
         * This {@code SpecType}'s string representation.
         */
        private String name;

        /**
         * Creates a new {@code SpecType}
         *
         * @param name the {@code SpecType}'s string representation.
         */
        private SpecType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}