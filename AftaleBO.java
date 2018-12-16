package dk.gensam.gaia.business;

import com.objectmatter.bsf.OQuery;
import dk.gensam.gaia.business.beregning.BeregningsDebugger;
import dk.gensam.gaia.business.beregning.OmkostningBeregner;
import dk.gensam.gaia.business.edi.panthaver.EdiPanthaverService;
import dk.gensam.gaia.business.oplysning.OplysningsAftjekWrapper;
import dk.gensam.gaia.business.oplysning.SammenhaengsvalideringTargetFinder;
import dk.gensam.gaia.business.pbs.PBSTilmeldingBO;
import dk.gensam.gaia.funktion.totalkunde.TotalkundeKontrolForstagerImpl;
import dk.gensam.gaia.funktion.totalkunde.TotalkundeKontrolHelper;
import dk.gensam.gaia.funktion.totalkunde.TotalkundetypeRegelManager;
import dk.gensam.gaia.integration.edi.EDIHelper;
import dk.gensam.gaia.integration.edi.database.EDIPanthaverdeklarationDb;
import dk.gensam.gaia.integration.edi.panthaver.EDIPanthaverDokument;
import dk.gensam.gaia.integration.edi.panthaver.Panthaverdeklarationstype;
import dk.gensam.gaia.integration.edi.panthaver.Panthaverdeklarationstype.Deklarationstype;
import dk.gensam.gaia.integration.gsxml.konfigurator.GSXMLBenaevnelseSerialiseringsKonfigurator;
import dk.gensam.gaia.integration.gsxml.serialisator.AftaleBOSerialisator;
import dk.gensam.gaia.integration.informationssystem.UdtraekInformationssystemHelper;
import dk.gensam.gaia.integration.naersikring.OverfoerselDokumentFacade;
import dk.gensam.gaia.model.adresse.*;
import dk.gensam.gaia.model.afregning.AfregningManager;
import dk.gensam.gaia.model.afregning.Afregningstype;
import dk.gensam.gaia.model.afregning.AftaleAfregningEjSamles;
import dk.gensam.gaia.model.afregning.AftaleArtp;
import dk.gensam.gaia.model.aftale.*;
import dk.gensam.gaia.model.aftale.Aftaletype.Forsikringstype;
import dk.gensam.gaia.model.egenskabsystem.*;
import dk.gensam.gaia.model.egenskabsystem.EgenskabSystem.EgenskabSammenhaenge;
import dk.gensam.gaia.model.gebyr.Gebyr;
import dk.gensam.gaia.model.gebyr.GebyrImpl;
import dk.gensam.gaia.model.gebyr.GebyrtypeRegel;
import dk.gensam.gaia.model.gebyr.GebyrtypeRegelImpl;
import dk.gensam.gaia.model.individ.*;
import dk.gensam.gaia.model.klausuler.*;
import dk.gensam.gaia.model.minpraemie.AftaleMinPrae;
import dk.gensam.gaia.model.minpraemie.AftaletypeMinPraeOplysning;
import dk.gensam.gaia.model.minpraemie.AftpMinPrae;
import dk.gensam.gaia.model.notesdataudsoegning.Notesdokumenttype;
import dk.gensam.gaia.model.notesdataudsoegning.NotesdokumenttypeImpl;
import dk.gensam.gaia.model.omkost.*;
import dk.gensam.gaia.model.omraade.*;
import dk.gensam.gaia.model.opgave.dagbog.DagbogAftale;
import dk.gensam.gaia.model.opgave.dagbog.Opgavetype;
import dk.gensam.gaia.model.opgave.dagbog.OpgavetypeImpl;
import dk.gensam.gaia.model.opgave.revidering.AftaleRev;
import dk.gensam.gaia.model.oplysning.Oplysning;
import dk.gensam.gaia.model.overblik.*;
import dk.gensam.gaia.model.pbs.*;
import dk.gensam.gaia.model.print.PrintjobPersistensService;
import dk.gensam.gaia.model.provision.ForsikringstagerAftalehaendelseProvisionmodtager;
import dk.gensam.gaia.model.rabat.AftaleRbtp;
import dk.gensam.gaia.model.rabat.AftaleRbtpImpl;
import dk.gensam.gaia.model.rabat.AftalekompRabattypeIF;
import dk.gensam.gaia.model.reassurance.ReaRisikoTp;
import dk.gensam.gaia.model.regelsaet.Sammenhaengsvalideringsregler;
import dk.gensam.gaia.model.relationer.*;
import dk.gensam.gaia.model.tilafgang.*;
import dk.gensam.gaia.model.totalkunde.AftaleTotalkundetype;
import dk.gensam.gaia.model.totalkunde.TotalkundeManager;
import dk.gensam.gaia.model.totalkunde.Totalkundetype;
import dk.gensam.gaia.model.util.*;
import dk.gensam.gaia.model.ydelse.*;
import dk.gensam.gaia.service.ForsikringWrapper;
import dk.gensam.gaia.service.ForsikringsService;
import dk.gensam.gaia.util.*;
import dk.gensam.gaia.util.dbserver.*;
import dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter;
import dk.gensam.gaia.util.sortering.ModelObjektGaeldendeComparator;
import dk.gensam.util.collection.OrderedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aftale businessobject, indkapsling af aftale og hovedprodukt model
 * objekteter. Klassen stiller følgende data til rådighed for brugeren:
 * <p>
 * <UL>
 * <LI> AftaleEgenskabsSystem
 * <LI> AftaleStatustyper
 * <LI> Klausuler
 * <LI> Status
 * <LI> Rabatter
 * <LI> Genstande
 * <LI> Afregningstyper
 * <LI> Hovedeforfald
 * <LI> Frekvens
 * <LI> Til/afgangsoplysninger
 * <LI> Aftale minimumsPræmie
 * <LI> Afregning fritekst
 * <LI> AftaleOmrådeRisikosted
 * </UL>
 * <p>
 * Fælles for dataene er at de først bliver loadet når de skal bruges, dvs.
 * første gang deres get metode kaldes.
 */

public class AftaleBO extends BusinessObject implements RelationsHolder, AdresseHolderBO, YdelsesHolderBO,
		DaekningHolderBO, KlausulHolderBO {
	private static Logger log_ = LoggerFactory.getLogger(AftaleBO.class);

	/**
	 * Udskriv debug information?
	 */
	public static final boolean DEBUG = false;

	private Aftale aftale;
	private AftaleEgenskabSystem aftaleEgenskabSystem;
	private List<KlausulBO> klausuler;

	private Hovedprodukttype[] hovedprodukttype_;
	private List<Hovedprodukt> hovedprodukt_;

	private boolean saveFremtidigeAdresser;

	// Initialisering af serialisatoren. Denne initialiserblock bliver kopieret ind i alle konstruktorer af javacompileren.
	// Det er derfor garanteret at det altid udføres som en del af konstruktorernes initialisering.
	{
		serialisator = new AftaleBOSerialisator(this);
	}

	/**
	 * Hvis initieret skal vi til at overføre forsikring mellem 2 selskaber
	 */
	public OverfoerselDokumentFacade requestDocumentAsFacade_;

	/**
	 * Initialiseres kun i funktionen
	 * {@link #setOphoer(BigDecimal, int)}.
	 * <p>
	 * <b>Default:</b> -1.
	 */
	private int ophoerAnnullerAendre_ = -1;

	/**
	 * Angiver om det er tilladt at bruge nye regelforslag o.l.
	 */
	private boolean medNyeRegler_ = false;
	/**
	 * True giver kun mening hvis medNyeRegler_ er false.
	 * Må ikke bruge andre nye regler end ikke-valgte dækninger
	 */
	private boolean udenNyeReglerMenMedDaekningsload_ = false;

	/**
	 * Hvis initieret har forsikringstager den alder - uanset indhold af cpr-nummer eller dato.<br>
	 * Kan f.eks. anvendes i en ui-sammenhæng, hvor bruger arbejder med en alder.
	 * Understøtter salgsmodul hvor hver bil har fører med hver sin alder
	 */
	protected BigDecimal alderNonPersistent = null;

	/**
	 * hasStatus gemmer information om selskabet gemmer status på deres aftaler.
	 * Som udgangspunkt antages det at alle aftaler har status, men hvis ikke
	 * opdateres variablen i {@link AftaleBO#loadStatus}.
	 *
	 * @deprecated ingen grund til skelnen, alt andet end panthaveraftaler har status
	 */
	@Deprecated
	private boolean hasStatus = true;
	/**
	 * Hack
	 * Hvis false, må vi ikke forsøge mix af load og persistering af aftalestatus
	 */
	private boolean statusLoadEnabled = true;
	private StatusBO[] status;
	private List<StatusBO> aftaleStatustyper;
	/**
	 * Hvis initieret anvendes den statustype til persistering af nye aftaler fremfor defaultregler.
	 */
	private Statustype statustypeToUseUansetHvad;

	private List<AftaleAfregningEjSamlesBO> aftaleAfregningEjSamles;
	/**
	 * Hvis ikke regelsætet forskriver tilladte forholdsbeskrivelser
	 * indeholder denne variabel værdien: <code>false</code>.
	 */
	private boolean hasRelationer = true;

	/**
	 * Liste over relationer tilknyttet aftalen. Indholdet er af typen
	 * {@link RelationsBO}. Relationerne vedligeholdes med to lister i
	 * modsætning til den almindelige måde, hvor en liste holder alle elementer.
	 */
	private List<RelationsBO> relationer;

	/**
	 * Individtyper der er tilladt i forhold til aftales type.
	 */
	private IntpAftp[] individtypeaftaletyper;
	private IntpAftpFhbsk[] forholdsbeskrivelser;

	/**
	 * Aftalens rabatter fra både data og regler.
     * Alle perioder excl. annullerede, da vi skal understøtte brug af dynamisk Tarifdato
	 */
	private List<RabatBO> rabatterAllePerioder;

	/**
	 * Aftalens genstande.
	 */
	protected List<GenstandBO> genstande;
	protected List<FaellesGenstandOplBO> faellesGenstandOpl;

	/**
	 * Aftalens afregningstyper
	 */
	protected List<AfregningstypeBO> afregningstyper;
    /**
     * Initieres af sammenhængsstyring
     */
    protected Set<Daekningstypekategori> selectableFiltre;

	/**
	 * Ydelser tilknyttet aftalen
	 */
	protected List<YdelseBO> ydelser;
	/**
	 * Betingede ydelser fra alle 4 niveauer Indeholder YdelseB=
	 */
	protected List<YdelseBO> ydelserBOBetingede;  // sættes ved save af ydelser

	private List<AdresseBO> adresser;
	private List<AdresseBO> tegnesAfAdresser;

	protected List<ForfaldBO> forfald;
	protected List<AftaleFftpMd> forfaldFremtidige;
	protected List<FrekvensBO> frekvens;
	protected List<AftaleFrekvens> frekvensFremtidige;

	private List<TilgangAfgangBO> tilAfgangOplysninger;
	private List<OmkFritagelseBO> omkFritagelser;
	private List<MinPraemieBO> minPraemie;
	private List<RisikostedBO> aftaleOmraadeRisikosted;

	private List<PbsDebitorgruppeBO> aftalePBSDebitorGruppeNr;

	private PBSTilmeldingBO pbsTilmelding;

	protected boolean isAnnulleret = false;
	protected boolean isNytTilbud = false;
	protected boolean isForsikringTilTilbud_;
	/**
	 * Konverteringshack for at disabled uønsket automatik
	 */
	protected boolean isDaekningstypeafhaengighederEnabled_ = true;

    /**
     * senest aflæste TarifdatoSubsidiaertRegelaflaesningsdato<br>
     * CPU-profilering påpeger behovet for caching af specielt tarifdato.
     */
    private BigDecimal tarifDatocached_ = null;

    /**
	 * Hvis disabled udføres ikke ydelsesberegning ved save
	 */
	protected boolean ydelsesBeregningEnabled = true;

	/**
	 * Hvis initieret er vi igang med en (bagud-)regulering og vi skal bruge periodeslut bagudperioden
	 */
	public BigDecimal reguleringSlutDato = null;

	/**
	 * True hvis vi er igang med at godkende et tilbud til at blive en forsikring.
	 * Er altså et tilbud, men skal valideres som en forsikring.
	 */
	protected boolean isVedAtGodkendeTilbud = false;

	protected AftaleAdresse[] fremtidigeLovligeAdresser;
	boolean hasFremtidigeAdresser_ = false; // Quick & dirty løsning på load af fremtidige adresser

	protected BonusrykningsDato bonusrykningsDato_ = null;

    /**
     * Hvis true har klienten disabled den automatiske flytning af dagbogsopgaver
     */
    private boolean dagsbogsflytningDisabled = false;
	/**
	 * Anvendes til validering af bo'er pr. type på tværs af parents
	 */
	private Set<BusinessObject> boerMarkedToValidatePrType = null;
	/**
	 * Angiver den angivne regelaflæsningsdato for aftalen om nogen
	 */
	private BigDecimal regelaflaesningsdatoUdefra_ = null;
	/**
	 * Angiver den beregnede regelaflæsningsdato for aftalen
	 */
	private BigDecimal regelaflaesningsdatoBeregnet_ = null;


	/**
	 * TIL EDI Panthaver, - hvis ophøret skyldes manglende betaling skal der sendes en anden type
	 * meddelse til panthaver end ellers.
	 */
	private boolean ophoerSkyldesManglendeBetaling = false;


	/**
	 * Hvis true har gui'en meddelt os, at vi er igang med at vognskifte - mest til ære for diplomordningen.
	 */
	private boolean vognskiftIgangForTotalkunde = false;

	protected boolean egenBetaltOmkValgt = false;
	protected boolean fuldRistornoValgt = false;

	private ArrayList<DaekningBO> daekningerRegAnnul;
	private ArrayList<DaekningBO> daekningerRegOphoer;
	/**
	 * En edi-ting til opsamling af alle dækninger der slettes fysisk
	 */
	private ArrayList<DaekningBO> daekningerRegSletning_;

	/**
	 * Holder styr på slettede ingnaf siden this blev instantieret.
	 */
	protected Set<InGnAf> slettedeDeklarationsRelationerThisAendring_ = null;

	/**
	 * Liste dækninger oprettet som nye hvis der er tilknyttet edi-panthaver til genstandsbo'et
	 */
	private List<DaekningBO> daekningerRegNye;

	/**
	 * Angiver om BO'et er en kopi af et andet.
	 */
	private boolean isKopi_;

	/**
	 * Angiver om BO'et er en TilAftaleBO ved Flyt Forsikring.
	 */
	protected boolean isTilAftaleVedFlytForsikring_;

	/**
	 * Hvis false er boet loaded uden persisterede genstande.
	 * Giver mest mening at anvende når !this.isNew()
	 */
	private boolean isLoadedMedGenstande = false;
	/**
	 * bruges ved serialisering af panthaverbreve ophør mht. overtagende selskab.
	 */
	private boolean tilOgAfgangeMedtagFremtidige = false;
	/**
	 * Resultat fra en explicit tilmelding Totalkunde
	 */
	private String resultTotalkundeTilmelding_;

	/**
	 * Variablen bruges til at skjule ophørsdato for children objekter,
	 * problemet er at forskellige objekter sætter tildato, selv om de ikke
	 * skal sættes til ophør. Introduceret i forbindelse med
	 * korttidsforsikring.
	 * <p>
	 *
	 * @todo Fjernes når alle BO-save metoder er implementeret korrekt, med
	 * hensyn til brugen af tilDato.
	 */
	protected BigDecimal tempTilDato;

	/**
	 * Reference til den instans der beregner omkostninger på aftaleniveau.<br>
	 * I skrivende stund, kan det kun være Stormflod
	 */
	private OmkostningBeregner omkostBeregner;

	private boolean isFrekvensSkift = false;
	private FrekvensBO frekvensSkiftBO;
	private FrekvensBO dftFrekvensBO;
	private boolean isHForfaldSkift = false;
	private ForfaldBO hfForfaldSkiftBO;

	/**
	 * @return en genbrugt eller nyinstantieret dummy instans - aldrig null
	 */
	public BeregningsDebugger getBeregningsDebugger() {
		if (beregningsDebugger_ == null)
			setBeregningsDebugger(false);
		return beregningsDebugger_;
	}

	/**
	 *
	 * @param debugOn  tænder og slukker for debuggeren (output til syslog)
	 */
	public void setBeregningsDebugger(boolean debugOn) {
		if (this.beregningsDebugger_ == null) {
			this.beregningsDebugger_ = new BeregningsDebugger();
		}
		this.beregningsDebugger_.DEBUG_ = debugOn;
	}

	/**
	 * Konfigurerer en ny debuggerinstans med tændt debug - output til til den angivne OutputFile.<br>
	 * Hvis initieret i forvejen ignoreres kaldet.
	 *
	 * @param filename - uden suffix
	 */
	public void setBeregningsDebugger(String filename) {
		if (this.beregningsDebugger_ == null) {
			this.beregningsDebugger_ = new BeregningsDebugger();
			this.beregningsDebugger_.DEBUG_ = true;
			this.beregningsDebugger_.setOutFile(filename);
		}
	}

	/**
	 * Skal initieres og konfigureres udefra hvis man ønsker trace / debug af prisberegning.
	 */
	public BeregningsDebugger beregningsDebugger_ = null;

	/**
	 * Variablen bruges til en work-around til at sætte fremtidige statustyper til ophør i forbindelse med ændring af ophør
	 * tilbage i tid.
	 * Problemet er at ved ændring af ophør får aftaleBO'et aftalens persistente oph.dato, og kender ikke den 'nye' ophørdato før
	 * til sidst i setOphoer() routinen. Derfor skal variablen gemme den 'nye' indtastede oph. dato, og udsøge statustyper efter
	 * denne i stedet for getDato(). F.eks. en aftales oph.dato ændres fra 01.06 til 01.08 og derefter til 01.05. Ved den
	 * sidste ændring kender aftaleBO'et kun 01.08 datoen, da det er aftalens persistente oph.dato, og vil derfor anvende denne
	 * dato til fremsøgning af fremtidige statustyper som skal sættes til ophør. I denne sitation vil der selvfølgelig ikke findes
	 * nogle - derfor skal 01.05 datoen anvendes i stedet, da det er den 'nye' oph dato aftaleBO'et vil få.
	 */
	private BigDecimal tempNyOphDato;

	/**
	 * Hvis sat anvendes den dato til load af egenskabssystemet.<br>
	 * Tiltænkt til at loade panthaveregenskabsystem for fremtidige panthaverBO'er
	 */
	private BigDecimal alternativLoadDato_;

	private BigDecimal cahcedOldOphoerTilFjern;

	/**
	 * Model over afregning fritekst tilknyttet aftalen.
	 */
	AfregningFritekstModel afregningFritekstModel;

	/**
	 * Markering for om der er vognskift.
	 */
	private boolean vognskiftIgang_ = false;

	/**
	 * Hvis sat udefra bruges den i stedet for defaults som Ændring og Nytegning
	 */
	private String aftalehaendelsestypeAlternativ_ = null;

	private String aftalehaendelseBemaerkning = null;

	/**
	 * Den evt. Aftalehaendelse der er gemt fra AftaleBO-objektet. Null hvis ingen gemt - endnu.
	 */
	private Aftalehaendelse aftalehaendelseSaved_;

	/**
	 * markering for om policeudskrift er fravalgt.
	 */
	private Boolean policeUdskriftFravalgt = null;

	/**
	 * Wrapperklasse brugt til DPN holdende ikkepersisterede data til forsikringen
	 **/
	private ForsikringWrapper forsikringWrapper;
	/**
	 * Typeenum brugt til DPN holdende ikkepersisterede data om typen af forsikringen
	 **/
	private Forsikringstype forsikringsart;
	private ForsikringsService forsikringsService;
	/**
	 * Hvis false skal tilgangsoplysninger opfattes som nye uden load af eksisterende data
	 */
	private boolean loadOpsigelserPersisterede = true;

	/**
	 * Er der fravalgt at få udskrevet policedokument på denne dato
	 */
	public boolean isPoliceUdskriftFravalgt(BigDecimal pDato) {
        if (policeUdskriftFravalgt != null) {
            return policeUdskriftFravalgt;
        }
        return aftale == null ? false : aftale.isPoliceUdskriftFravalgt(pDato);
    }

	/**
	 * Sætter hvorvidt udskrivning af policedokument skal udføres
	 */
	public void setPoliceUdskriftFravalgt(boolean pPoliceUdskriftFravalgt) {
		this.policeUdskriftFravalgt = Boolean.valueOf(pPoliceUdskriftFravalgt);
	}

	private boolean skipSaveAftalehaendelse_ = false;

	ProvisionsModtagerSaetHolder provModtagerSaetHolder = null;

	/**
	 * alternativ satsdato hvor defaults ikke skal gælde
	 */
	private BigDecimal satsDatoAlternativ_;
	/**
	 * Hvis false gemmes aftalen ikke ved save hvis der ikke er sket noget med aftalen.
	 */
	private boolean gemEksisterendeAftaleEntitet = true;

	private boolean undladOmraadeKonsekvensVedSaveAll = false;
	private boolean isUdfoerIndexRegulering = false;
	private BigDecimal grunddatoForIndexRegulering;

	private boolean isForlaengKorttid = false;
	private BigDecimal forlaengKorttidFraDato = null;
	private BigDecimal forlaengKorttidTilDato = null;
	private List<GenstandObjectIF> forlaengKorttidGenstandeTilOphoer = null;

	private BigDecimal totalkundeKontrolDatoVedFjernOphoer = null;

	private int nyStatusTypeCaller = -1;
	private String nyStatusTypeCallerTekst = "";

	/**
	 * Angiver om børn har trigget en ydelsesberegning
	 */
	private boolean ydelsesberegningBestilt_ = false;

	/**
	 * Angiver om edi-kommunikation er en- eller disabled på denne instans.
	 */
	private boolean ediKommunikationEnabled_ = true;

	/**
	 * For PanthaverdeklarationsBO'er sættes det EDIPanthaverDokument, den skal tilknyttes ved save
	 */
	private EDIPanthaverdeklarationDb ediPanthaverdeklarationDb_;

	/**
	 * Hvis initieret er vi en panthaverdeklarationsbo med E-deklaration hvor bruger har sat de ønskede acceptkoder
	 */
	private String[] ediDeklarationstype_E_acceptkoder;

	// boolean til at undlade totalkundekontrol for bornholm ved index og bonusregulering
	private boolean undladTotalkundeKontrol = false;

    // boolean til at undlade aftalebo specifikke revideringer
	private boolean undladUdvalgteRevideringer = false;

	/**
	 * Hvis defineret kan der aflæses adfærdsstraegier ved load og andet
	 */
	private BusinessObjectAdfaerdStrategy adfaerdStrategy_;

    /**
     * Hvis klienten ved, at den ikke vil bruge individets adresser til noget, kan meget arbejde spares ved at sætte denne til true
     * Nej, ændret til at klienten skal anfordre individets adresser
     */
	protected boolean skipLoadIndividetsAdresser = true;

	private boolean foedesSomPBS_ = false;

	private SammenhaengsregelHelper sammenhaengsregelHelper = null;

	/**
	 * Hvis meoden kaldes før load bliver den nye forsikring født med Afregningstype PBS
	 */
	public void setFoedesSomPBS() {
		foedesSomPBS_ = true;
	}

	public void setFoedesSomPBSOff() {
		foedesSomPBS_ = false;
	}

	public boolean isUndladTotalkundeKontrol() {
		return undladTotalkundeKontrol;
	}

	public void setUndladTotalkundeKontrol(boolean pUndladTotalkundeKontrol) {
		this.undladTotalkundeKontrol = pUndladTotalkundeKontrol;
	}

	public void setEDIPanthaverDokument(EDIPanthaverdeklarationDb pEDIPanthaverdeklarationDb) {
		ediPanthaverdeklarationDb_ = pEDIPanthaverdeklarationDb;
		if (ediPanthaverdeklarationDb_.isAnvendt()) {
			ediPanthaverdeklarationDb_ = null;
			/* Tidligere GensamUtil.msg ... */
			log_.info(GensamUtil.getMemLabel() + "AftaleBO anvendelse af anvendt edipanthaverdeklaration ");
		}
	}

	private boolean minimumLoadAfYdelser = false;

	public AftaleBO(Aftaletype type, BusinessObject parent, BigDecimal dato) {
		super(type, parent, dato);
		type_ = type;
//		log_.info("type: " + type);
		initSammenhaengsregelHelper();
		loadHovedprodukt();
		if (parent instanceof IndividBO) {
			((IndividBO) parent).setAftaleBOCurrent(this);
		}

	}

	public AftaleBO(ModelObjekt entitet, BusinessObject parent, BigDecimal dato) {
		super(entitet, parent, dato);
		aftale = (Aftale) getEntitet();
		type_ = aftale.getAftaleTypen();
		initSammenhaengsregelHelper();
		loadHovedprodukt();
		isAnnulleret = aftale.isAnnulleret();
		if (parent instanceof IndividBO) {
			((IndividBO) parent).setAftaleBOCurrent(this);
		}
	}

	private void initSammenhaengsregelHelper() {
		sammenhaengsregelHelper = new SammenhaengsregelHelper();
	}

	protected final SammenhaengsregelHelper getSammenhaengsregelHelperDo() {
		boolean isKonv = adfaerdStrategy_ != null &&
				adfaerdStrategy_.isGsproGsproKonvertering();
		if (!isKonv)
			return sammenhaengsregelHelper;
		return null;
	}

	public void setUndladOmraadeKonsekvensVedSaveAll(boolean pUndladOmraadeKonsekvensVedSaveAll) {
		undladOmraadeKonsekvensVedSaveAll = pUndladOmraadeKonsekvensVedSaveAll;
	}

	public boolean isUndladOmraadeKonsekvensVedSaveAll() {
		return undladOmraadeKonsekvensVedSaveAll;
	}

	/**
	 * Beder alle YdelseBO'er om at regulere sig selv
	 *
	 * @param pGrunddato        Mindstedatoen for Indextal (og Aftale) ved regulering af Fremtidige aftaler
	 * @param pDelvisRegulering
	 */
	public void udfoerIndexRegulering(BigDecimal pGrunddato, boolean pDelvisRegulering) {
		// Gør information tilgæmgelig for YdelseBO
		isUdfoerIndexRegulering = true;
		grunddatoForIndexRegulering = pGrunddato;

		setMinimumLoadAfYdelser(true);

		// Reguler alle -- de ved selv om og hvordan
		List<YdelseBO> ydelserList = this.getAlleValgteYdelserUnderAftalen();
		if (ydelserList != null) {
			for (YdelseBO ydelseBO : ydelserList) {
				if (pDelvisRegulering) {
					ydelseBO.udfoerIndexReguleringDelvis();
				} else {
					ydelseBO.udfoerIndexRegulering();
				}
			}
		}
		// Reguler ikke-ydelser -- de ved også selv om og hvordan
		if (((Aftaletype) type_).getIndekstype(getDato()) != null) {
			List<MinPraemieBO> minPraemieList = this.getMinPraemie();
			if (minPraemieList != null) {
				for (MinPraemieBO minPr : minPraemieList) {
					minPr.udfoerIndexRegulering();
				}
			}
		}
		this.setGemEksisterendeAftaleEntitet(false);
		this.setAftalehaendelsestypeAlternativ(null);
		saveAll();
	}

	/**
	 * @return true hvis dette BO allerede er markeret til ophør
	 */
	public boolean isMarkedOphoer() {
		return tempNyOphDato != null;
	}

	public boolean isUdfoerIndexRegulering() {
		return isUdfoerIndexRegulering;
	}

	public void setIsKopi(boolean pIsKopi) {
		isKopi_ = pIsKopi;

	}

	public boolean isKopi() {
		return isKopi_;
	}

	public void setIsTilAftaleVedFlytForsikring(boolean pIsTilAftaleVedFlytForsikring) {
		isTilAftaleVedFlytForsikring_ = pIsTilAftaleVedFlytForsikring;
	}

	public boolean isTilAftaleVedFlytForsikring() {
		return isTilAftaleVedFlytForsikring_;
	}

	public void setEgenbetaltOmk(boolean pValg) {
		egenBetaltOmkValgt = pValg;
	}

	public void setFuldRistornoValgt(boolean pValg) {
		fuldRistornoValgt = pValg;
	}

	public void destroy() {
		super.destroy();
		frekvens = null;  // forsøg for at undgå mem.leaks
		forfald = null;
		faellesGenstandOpl = null;
		genstande = null;
		tilAfgangOplysninger = null;
	}


	/**
	 * AddMaegler er genvejen fra det gamle framework der arbejder direkte på
	 * Modelobjekter til Businessobject'er. Hver gang mægler-gui der vælges en
	 * mægler tilføjes denne her og der sørges samtidlig med at samme mægler
	 * ikke allerede er valgt og at kun denne ene mægler er valgt.
	 * <P>
	 * Proceduren vedligeholder listen af mæglere og ved hver kald indsættes og
	 * vælges en ny mægler i listen, hvis den ikke findes i forvejen.
	 * <P>
	 * <Strong>Bemærkning:</Strong><BR>Proceduren omgåes constructorne i
	 * RelationsBO ved at kalde {@link BusinessObject#setIsNew(boolean)}.
	 * <P>
	 * @param Individ       Relationens individ
	 * @param IndividAftale Sammenhængsbeskrivelse for individ og aftale.
	 * @param boolean       selected på grund af denne <I>bøje-søm-løsning</I>
	 *                      er det nødvendigt at kunne styre om relationen er
	 *                      valgt.
	 * @param boolean       isNew ditto.
	 *
	 * @return Det berørte RelationsBO
	 *//*
	public RelationsBO addMaegler(Individ                   individ,
								  IndividAftale             inaf,
								  ForholdsbeskrivelseIF     forholdsbeskrivelse) {

		RelationsBO rbo = null;
		boolean found = false;
*/
	/**
	 * Findes der allerede et RelationsBO med det valgte individ?
	 * Først når det er afgjort kan der vælges constructor til det nye
	 * RelationsBO fordi initialisering af status-attributterne håndteres
	 * fra constructor, og for ikke at skulle bøje flere søm i denne
	 * løsning er det vigtigt at statusattributterne er på plads til save.
	 *//*
		if(individ != null) {
            for (int i = 0; !found && i < maegler.size(); i++) {
                rbo = (RelationsBO)maegler.get(i);
                if(rbo.getEntitet().getId().equals(individ.getId())) {
                    found = true;
                    break;
                }
            }
        }*/

	/**
	 * Fjern selection på en eventuel eksisterende. Med fare for at samme bo
	 * først fravælges og så i anden del af nedenstående if vælges igen.
	 *//*
		RelationsBO selectedBO = (RelationsBO)getSelected(maegler);
		if (selectedBO != null) {
			selectedBO.setSelecteret(false);
		}

		if (!found && individ != null) {
			rbo = new RelationsBO(individ, this, inaf,
								  getDato(), true, true, forholdsbeskrivelse);
			maegler.add(rbo);

		} else {*/

	/**
	 * Der findes en relation (rbo) der indeholder individ parameteren,
	 * den har været valgt før.. rbo indeholder den rigtige relation
	 *//*
			if (rbo != null) {
				rbo.setSelecteret(true);
			}
		}

		return rbo;*/
//	}

	/*
	 * Initialisering med isSelecteret() == boolean-argument.
	 * <P>
	 * @see AftaleBO#addmaegler(Individ, IndividAftale)
	 *//*
	public RelationsBO addMaegler(Individ                   individ,
								  IndividAftale             inaf,
								  boolean                   selected,
								  boolean                   isNew,
								  ForholdsbeskrivelseIF     forholdsbeskrivelse) {

		RelationsBO rbo = addMaegler(individ, inaf, forholdsbeskrivelse);

		if (rbo != null) {
			rbo.initSelecteret(selected);
			rbo.setIsNew(isNew);
		}

		return rbo;
	}*/
	private void loadHovedprodukt() {
		hovedprodukt_ = new ArrayList<Hovedprodukt>();
		ArrayList<Hovedprodukttype> hovedprodukttyper = new ArrayList<Hovedprodukttype>();
		if (!isNew()) {
			Object[] o = ((AftaleImpl) getEntitet()).getHovedprodukt();
			if ((o != null) && (o.length > 0)) {
				hovedprodukttype_ = new Hovedprodukttype[o.length];
				for (int i = 0; i < o.length; i++) {
					hovedprodukt_.add((Hovedprodukt) o[i]);
					hovedprodukttyper.add(((Hovedprodukt) o[i]).getHovedprodukttype());
				}
			}
		}
		hovedprodukttype_ = null;
		if (getDato() != null && getType() != null)
			hovedprodukttype_ = ((Aftaletype) getType()).getHovedprodukttypeGld(getDato());
		for (int i = 0; hovedprodukttype_ != null && i < hovedprodukttype_.length; i++) {
			if (!hovedprodukttyper.contains(hovedprodukttype_[i]))
				hovedprodukttyper.add(hovedprodukttype_[i]);
		}
		hovedprodukttype_ = (Hovedprodukttype[]) ContainerUtil.toArray(hovedprodukttyper);
	}

	public Hovedprodukt getHovedprodukt(Hovedprodukttype pHovedprodukttype) {
		if (hovedprodukt_ != null) {
			for (int i = 0; i < hovedprodukt_.size(); i++) {
				if ((hovedprodukt_.get(i)).getHovedprodukttype().equals(pHovedprodukttype))
					return hovedprodukt_.get(i);
			}
		}
		return null;
	}

	public void setHovedprodukttype(RegelsaetType pHovedprodukttype) {
//		Hovedprodukttype[]   hovedprodukttype_;
		if (hovedprodukttype_ == null || hovedprodukttype_.length != 1) {
			throw new UnsupportedOperationException("Kan kun udskifte hovedpdtp på aftaler med 1 hvpdtp");
		}
		hovedprodukttype_[0] = (Hovedprodukttype) pHovedprodukttype;
	}

	public Hovedprodukttype getHovedprodukttype() {
//		Hovedprodukttype[]   hovedprodukttype_;
		if (hovedprodukttype_ != null && hovedprodukttype_.length != 1) {
			throw new UnsupportedOperationException("Kan kun gette hovedpdtp på aftaler med 1 hvpdtp");
		}
		return hovedprodukttype_[0];
	}

	/**
	 * Lister der har været brugt i forbindelse med NavigationsPanel kan
	 * indeholde elementer med typen Blank. Denne funktion fjerner dem.
	 */
	protected void removeBlank(List l) {
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i) instanceof Blank) {
				l.remove(i);
			}
		}
	}

	/**
	 * Returnere det integer flag som aftaleBO'et kan ha fået ved ophør.
	 */
	public int getOphoerAnnullerAendreStatus() {
		return ophoerAnnullerAendre_;
	}

	/**
	 * Metode som returnerer alle valgte dækningstyper på AftaleBO'ets genstande.
	 * Loader kun det som endnu ikke er loaded.
	 */
	public List<Produkttype> getValgteDaekningstyper() {
		List<GenstandBO> genst = getGenstande();
		List<Produkttype> valgteDaekningstyper = new ArrayList<Produkttype>();

		if (genst != null && genst.size() > 0) {
			for (int g = 0; g < genst.size(); g++) {
				if ((genst.get(g)).isSelecteret()) {
					List<DaekningBO> daekninger = (genst.get(g)).getDaekninger();
					if (daekninger != null && daekninger.size() > 0) {
						for (int d = 0; d < daekninger.size(); d++) {
							if ((daekninger.get(d)).isSelecteret()) {
								if (!(valgteDaekningstyper.contains((Produkttype) (daekninger.get(d)).getType()))) {
									valgteDaekningstyper.add((Produkttype) (daekninger.get(d)).getType());
								}
							}
						}
					}
				}
			}
		}
		return valgteDaekningstyper;
	}

	/**
	 * Metode som returnere alle de valgte dækninger på AftaleBO'ets genstande.
	 * (SHS) ADVARSEL: Du risikerer en reload/reinstantiering af GenstandBO/DaekningBO og
	 * dermed mistede referencer til andre xxxxxBO
	 */
	public List<DaekningBO> getValgteDaekninger() {
		List<GenstandBO> genst = null;

		if (isNew() || isForlaengKorttid) {
			genst = genstande;
		} else {
			genst = getGaeldendeOgFremtidigeGenstande();
		}

		List<DaekningBO> valgteDaekninger = new ArrayList<DaekningBO>();

		if (genst != null && genst.size() > 0) {
			for (int g = 0; g < genst.size(); g++) {
				if ((genst.get(g)).isSelecteret()) {

					List<DaekningBO> daekninger = null;
					if (isNew() || isForlaengKorttid) { /** @todo til GenstandsBO */
						daekninger = (genst.get(g)).getDaekninger();
					} else {
						daekninger = (genst.get(g)).getGaeldendeOgFremtidigeDaekninger();
					}


					if (daekninger != null && daekninger.size() > 0) {
						for (int d = 0; d < daekninger.size(); d++) {
							if ((daekninger.get(d)).isSelecteret()) {
								valgteDaekninger.add((daekninger.get(d)));
							}
						}
					}
				}
			}
		}
		return valgteDaekninger;
	}


	/**
	 * Metode som returnere alle dækninger på AftaleBO'ets selekterede genstande.
	 *
	 * @return alle dækninger på selekterede genstande
	 */
	public List<DaekningBO> getAlleDaekninger() {
		return getAlleDaekninger(true);
	}

	@Override
	public List<DaekningBO> getDaekningerSelekteredeGenstande() {
		return getAlleDaekninger();
	}

	@Override
	public List<DaekningBO> getDaekningerSelekteredeDaekninger() {
		List<DaekningBO> retur = new ArrayList<>();
		List<DaekningBO> daekningerSelekteredeGenstande = getDaekningerSelekteredeGenstande();
		if (daekningerSelekteredeGenstande != null) {
			for (DaekningBO daekning : daekningerSelekteredeGenstande) {
				if (daekning.isSelecteret()) {
					retur.add(daekning);
				}
			}
		}
		return retur;
	}

	/**
	 * @param kunSelekteredeGenstande, false = dækninger på alle genstande
	 * @return
	 */
	public List<DaekningBO> getAlleDaekninger(boolean kunSelekteredeGenstande) {
		List<GenstandBO> genstande = getGenstande();

		List<DaekningBO> alleDaekninger = new ArrayList<>();

		if ((genstande != null) && (genstande.size() > 0)) {
			for (int g = 0; g < genstande.size(); g++) {
				if ((genstande.get(g)).isSelecteret() || !kunSelekteredeGenstande) {
					List<DaekningBO> daekninger = (genstande.get(g)).getDaekninger();
					if ((daekninger != null) && (daekninger.size() > 0)) {
						for (int d = 0; d < daekninger.size(); d++) {
							alleDaekninger.add(daekninger.get(d));
						}
					}
				}
			}
		}
		return alleDaekninger;
	}

	private BigDecimal getHovedforfaldDatoForNyAftale() {
		BigDecimal hovedforfaldDato = null;
		ArrayList forfaldList = (ArrayList)getForfald();
		for (int i = 0; i < forfaldList.size(); i++) {
			if ((((ForfaldBO)forfaldList.get(i)).isSelecteret()) &&
					(((ForfaldBO)forfaldList.get(i)).getType().getKortBenaevnelse().trim().equals("HOVED FF"))) {
				int datoAar = getDato().intValue() / 10000;
				int wDato = (datoAar * 10000) + (((ForfaldBO)forfaldList.get(i)).getMaanednr().intValue() * 100) + 01;
				hovedforfaldDato = new BigDecimal(wDato);
				while (!(hovedforfaldDato.compareTo(getDato()) > 0)) {
					hovedforfaldDato = hovedforfaldDato.add(new BigDecimal(10000.0)); // Læg 1 år til (HF-frekvens er altid 1 år)
				}
			}
		}
		return hovedforfaldDato;
	}

    /**
     *
     * @return en liste med aftalens realiserede ydelser, tom liste hvis ingen, aldrig null
     */
	List<YdelseBO> getYdelseBORealiserede() {
        List<YdelseBO> retur = new ArrayList<>();
        List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
        for (GenstandBO genstandBO : genstandeSelekteret) {
            List<YdelseBO> ydelser = genstandBO.getYdelser();
            for (YdelseBO ydelseBO : ydelser) {
                if (ydelseBO.isSelecteret()) {
                    boolean isRealiseret = ydelseBO.getType().isRealiseretYdelse(getDato());
                    if (isRealiseret && !ydelseBO.getType().isBeregnet(getDato())) {
                        retur.add(ydelseBO);
                    }
                }
            }
        }
        return retur;
    }

	/**
	 * Arbejdsskadeforsikringer overført til Nærsikring og behandlet i Nærsikring. <br>
	 * Hvis ændring stammer fra en bagudregulering så skal reguleringSlutdato må ikke være null. <br>
	 * Ellers bliver isBeregnetViaRegulering-realiserede-ydelser ikke beregnet i Nærsikring ifbm overførsel fra andre selskaber. <br>
	 * Dvs. hvis der bare findes en "normal" realiseret ydelse med samme fradato så skal der sættes reguleringSlutdato for at der beregnes de 2 "isBeregnetViaRegulering-realiserede-ydelser" . <br>
	 *   "isBeregnetViaRegulering-realiserede-ydelser" er "Netto til-/afgang heltidsansat" og "Arb: real. heltidsansatte ialt" . <br>
	 *    Andre realiserede ydelser ses i metoden.
	 *
	 * TODO Hvad så når det er ren ændrinng? Gør det noget at der forsøges at beregne realiserede ydelser?
	 */
	private void tjekOgEvtSaetReguleringSlutDato() {
		BigDecimal datoToUse = getDato();
		if (fraDato_ != null && fraDato_.compareTo(datoToUse) > 0) {
			datoToUse = fraDato_;
		}

        List<YdelseBO> realiseredeYdelser = getYdelseBORealiserede();
        boolean reguleringSlutdatoSkalFindes = isReguleringSlutdatoSkalFindes(datoToUse, realiseredeYdelser);
        if (!reguleringSlutdatoSkalFindes) {
			return;
		}
        setReguleringSlutDato(datoToUse);
        if (reguleringSlutDato != null){
            overfoerSlutdatoTilRealiserede(realiseredeYdelser);
        }
    }

	/**
	 * Der sættes reguleringSlutdato på realiseredeYdelser.
	 * @param realiseredeYdelser
	 */
    private void overfoerSlutdatoTilRealiserede(List<YdelseBO> realiseredeYdelser) {
	    for (YdelseBO ydelse : realiseredeYdelser){
            log_.info("Realiseret ydelse har fået sat tildato " + reguleringSlutDato + "  " + ydelse.toString());
	        ydelse.setTilDato(reguleringSlutDato);
        }
    }

	/**
	 * Set evt. reguleringSlutdato som kan være en fornyelsesdato lige efter "datoToUse" eller en forsikringensophørsdato.
	 * @param datoToUse
	 */
    private void setReguleringSlutDato(BigDecimal datoToUse) {
        BigDecimal naestFornyelsesDato = null;
        if (aftale != null) {
            DagbogAftale naesteFornyelsesOpgaveEfterFradato = aftale.getFoerstKommendeDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), null, Datobehandling.datoPlusMinusAntal(datoToUse, +1));
            if (naesteFornyelsesOpgaveEfterFradato != null) {
                naestFornyelsesDato = naesteFornyelsesOpgaveEfterFradato.getGld();
                naestFornyelsesDato = Datobehandling.datoPlusMinusAntal(naestFornyelsesDato, -1);
                if (aftale.isOphoert() && aftale.getOph().compareTo(naestFornyelsesDato) < 0) {
                    naestFornyelsesDato = aftale.getOph();
                }

                reguleringSlutDato = naestFornyelsesDato;
            }
        }else{  //det kan være en genoptaget aftale som bliver overført f.eks. en med hændelse "OPRET_AFLOESER" - Og denne genoptagede aftale havde været reguleret før den var sat til ophørt.
            naestFornyelsesDato = getHovedforfaldDatoForNyAftale();
            if (naestFornyelsesDato != null) {
                reguleringSlutDato = Datobehandling.datoPlusMinusAntal(naestFornyelsesDato, -1);;
            }
        }
    }

	/**
	 * @param datoToUse
	 * @param realiseredeYdelser
	 * @return true hvis der bare findes en af de "realiseredeYdelser" med samme fradato som  "datoToUse". Ellers false.
	 */
    private boolean isReguleringSlutdatoSkalFindes(BigDecimal datoToUse, List<YdelseBO> realiseredeYdelser) {
        for (YdelseBO ydelseBO : realiseredeYdelser) {
            BigDecimal datoToUse2 = ydelseBO.getDato();
            if (ydelseBO.getFraDato() != null && ydelseBO.getFraDato().compareTo(datoToUse2) > 0) {
                datoToUse2 = ydelseBO.getFraDato();
            }
            if (datoToUse2.compareTo(datoToUse) == 0) {
                return true;
            }

        }
        return false;
    }

    /**
	 * Udfør beregning af alle beregnede ydelsesbo'er på this og alle børn
	 */
	public final void beregnAlleBeregnedeYdelserUnderAftalen() {
		final List<YdelseBO> alleBeregnedeYdelserUnderAftalen = getAlleBeregnedeYdelserUnderAftalen();
		for (YdelseBO ydbo : alleBeregnedeYdelserUnderAftalen) {
			ydbo.setIsBeregnetOff();
		}

		if (reguleringSlutDato == null && DBServer.getInstance().getDatabase().startsWith("NE") &&
                getType().getKortBenaevnelse().equals(Aftaletype.Forsikringstype.ARB.getKortBenaevnelse())) {
			if ((aftale != null && aftale.isElektroniskOverfoert()) || aftale == null) {
				tjekOgEvtSaetReguleringSlutDato();
			}
		}

		for (YdelseBO ydbo : alleBeregnedeYdelserUnderAftalen) {
			ydbo.beregnAngivelse(getRegelaflaesningsdato(getDato()), reguleringSlutDato);
		}
	}

	/**
	 * En specialindgang til at begrænse saveAll til ydelser
	 */
	public final void saveAlleYdelserUnderAftalen() {
		final List<YdelseBO> alleYdelserMedParentSelected = getAlleYdelserUnderAftalen();
		for (YdelseBO ydbo : alleYdelserMedParentSelected) {
			ydbo.saveAll();
		}
	}

	/**
	 * @return liste med alle aftalens beregnede ydelsesbo'er med selekteret parent, tom liste hvis ingen.
	 * Kan indholde flere perioder.
	 */
	public final List<YdelseBO> getAlleBeregnedeYdelserUnderAftalen() {
		List<YdelseBO> result = new ArrayList<>();
		final List<YdelseBO> childrenAll = getAlleYdelserUnderAftalen();
		if (childrenAll != null) {
			for (YdelseBO bo : childrenAll) {
				Ydelsestype ydtp = bo.getType();
				if (ydtp.isBeregnet(getDato()) && bo.getParent().isSelecteret()) {
					boolean add = true;
					if (bo.getParent() instanceof GenstandBO) {
						GenstandBO gnbo = (GenstandBO) bo.getParent();
						List<DaekningBO> selekteredeDaekninger = gnbo.getDaekningerSelekteredeDaekninger();
						add = !selekteredeDaekninger.isEmpty();
						// Denne regel må gælde lidt implicit, da Key-programmet er baseret på afspejlede ydelser til dækningsniveau.
					}
					if (bo.getParent() instanceof DaekningBO) {
						if (!bo.getParent().getParent().isSelecteret())
							add = false;
						// Dækningen er selekteret, men gentanden er ikke - skal ikke med
					}
					if (add)
						result.add(bo);
				}
			}
		}
		return result;
	}

	/**
	 * Hvis der for persisteret aftale findes fremtidige tarifdatoer, udlægges hver enkelt ydelse i argumentet for hver dato med
	 * egen og ny dato, fra- og tildato  og regelaflæsningsdato.
	 *
	 * @param eenPeriode
	 * @return ny liste med en instans af hver pr. tarifdato eller argumentet uændret hvis ingen fremtidige
	 */
	private List<YdelseBO> klonTilFremtidigeTarifDatoer(List<YdelseBO> eenPeriode) {
		if (!isNew() && getAftale() != null) {
			BigDecimal[] fremtidigeTarifDatoer = getAftale().findFremtidigeTariferingsDatoer(getDato());
			// SKAL være fremtidige og sorteret kronologisk
			if (fremtidigeTarifDatoer != null) {
				List<YdelseBO> retur = new ArrayList<>(eenPeriode.size() * fremtidigeTarifDatoer.length);
				retur.addAll(eenPeriode);
				for (int i = 0; i < fremtidigeTarifDatoer.length; i++) {
					for (YdelseBO ydbo : eenPeriode) {
						if (i == 0) {
							BigDecimal slutDenOprindelige = Datobehandling.datoPlusMinusAntal(fremtidigeTarifDatoer[0], -1);
							ydbo.setTilDato(slutDenOprindelige);
						}
						BigDecimal start = fremtidigeTarifDatoer[i];
						BigDecimal slut = BigDecimal.ZERO;
						if (i < (fremtidigeTarifDatoer.length - 1)) {
							slut = Datobehandling.datoPlusMinusAntal(fremtidigeTarifDatoer[i + 1], -1);
						}

						YdelseBO ydNy = (YdelseBO) ydbo.clone();
						ydNy.setDato(start);
						ydNy.setFraDato(start);
						ydNy.setTilDato(slut);
						regelaflaesningsdatoBeregnet_ = null; // fjerner cached dato, da vi kan komme ind i nyt forsikringsår her
						ydNy.setRegelAflaesningsDatoToUse(getRegelaflaesningsdato(start));
						ydbo.getParent().addYdelseBO(ydNy); // Adder klonen til holder som child på lige fod med alle andre children
						retur.add(ydNy);

					}
				}
				regelaflaesningsdatoBeregnet_ = null; // fjerner cached dato, da den kan være ændret ovenfor
				return retur;
			}
		}

		return eenPeriode;
	}

	/**
	 * @return liste med alle ydelser på this, valgte genstande og valgte dækkninger på valgte genstande. Tom liste hvis ingen - aldrig null
	 */
	public List<YdelseBO> getAlleYdelserUnderAftalen() {
		ArrayList<YdelseBO> result = new ArrayList<>();
		// Aftale ydelser
		result.addAll(getYdelser());

		// Genstands ydelser
		ArrayList<? extends BusinessObject> ydelsesHolder = (ArrayList) getGenstande();
		for (int i = 0; i < ydelsesHolder.size(); i++) {
			if (ydelsesHolder.get(i).isSelecteret()) {
				((YdelsesHolderBO) ydelsesHolder.get(i)).setMinimumLoadAfYdelser(isMinimumLoadAfYdelser());
				result.addAll(ydelsesHolder.get(i).getYdelser());
			}
		}

		// Dæknings ydelser
		ArrayList genstande = (ArrayList) getGenstande();
		for (int i = 0; i < genstande.size(); i++) {
			if (((BusinessObject) genstande.get(i)).isSelecteret()) {
				ydelsesHolder = (ArrayList) ((GenstandBO) genstande.get(i)).getDaekninger();
				for (int j = 0; j < ydelsesHolder.size(); j++) {
					if (ydelsesHolder.get(j).isSelecteret()) {
						((YdelsesHolderBO) ydelsesHolder.get(j)).setMinimumLoadAfYdelser(isMinimumLoadAfYdelser());
						result.addAll(ydelsesHolder.get(j).getYdelser());
					}
				}
			}
		}
		return result;
	}

	public List<YdelseBO> getAlleValgteYdelserUnderAftalen() {
		ArrayList<YdelseBO> result = new ArrayList<>();
		// Aftale ydelser
		result.addAll(getValgteYdelser(getYdelser()));

		// Genstands ydelser
		ArrayList ydelsesHolder = (ArrayList) getGenstande();
		for (int i = 0; i < ydelsesHolder.size(); i++) {
			if (((BusinessObject) ydelsesHolder.get(i)).isSelecteret()) {
				((YdelsesHolderBO) ydelsesHolder.get(i)).setMinimumLoadAfYdelser(isMinimumLoadAfYdelser());
				result.addAll(getValgteYdelser(((GenstandBO) ydelsesHolder.get(i)).getYdelser()));
			}
		}

		// Dæknings ydelser
		ArrayList genstande = (ArrayList) getGenstande();
		for (int i = 0; i < genstande.size(); i++) {
			if (((BusinessObject) genstande.get(i)).isSelecteret()) {
				ydelsesHolder = (ArrayList) ((GenstandBO) genstande.get(i)).getDaekninger();
				for (int j = 0; j < ydelsesHolder.size(); j++) {
					if (((BusinessObject) ydelsesHolder.get(j)).isSelecteret()) {
						((YdelsesHolderBO) ydelsesHolder.get(j)).setMinimumLoadAfYdelser(isMinimumLoadAfYdelser());
						result.addAll(getValgteYdelser(((DaekningBO) ydelsesHolder.get(j)).getYdelser()));
					}
				}
			}
		}
		return result;
	}

	private List<YdelseBO> getValgteYdelser(List<YdelseBO> pYdelser) {
		ArrayList<YdelseBO> result = new ArrayList<YdelseBO>();
		for (int i = 0; i < pYdelser.size(); i++) {
			if ((pYdelser.get(i)).getAngivelse() != null)
				result.add(pYdelser.get(i));
		}
		return result;
	}


	/**
	 * Returnerer en liste der indholder samtlige businessobjecter der findes i
	 * AftaleBO.
	 */
	public List getChildren() {
		ArrayList children = new ArrayList();
		if (!(getParent() instanceof PanthaverBO)) {
			if (hasStatus()) children.addAll(ContainerUtil.asList(status));
			if (aftaleStatustyper != null) children.addAll(aftaleStatustyper);

			// Mæglerlisten er aldrig null
//			children.addAll(maegler);

			if (adresser != null) {
				removeBlank(adresser);
				children.addAll(adresser);
			}

			// Korttidsaftaler skal ikke have gemt hovedforfald og frekvens
			if (forfald != null) children.addAll(forfald);
			if (frekvens != null) children.addAll(frekvens);

			if (relationer != null) {
				removeBlank(relationer);
				children.addAll(relationer);
			}

			if (ydelser != null)
				children.addAll(ydelser);

			if (genstande != null) {
				children.addAll(getFaellesGenstandOplysninger());
				children.addAll(getGenstande());
			}

			if (pbsTilmelding != null) {
				// OBS vigtigt at den ligger før Afregningstyperne i save-sekvensen, da der kan blive reselekteret
				children.add(getPBSTilmelding());
			}

			if (afregningstyper != null) {
				// OBS vigtigt at den ligger efter PBSTilmelding i save-sekvensen
				children.addAll(getAfregningstyper());
			}

			if (rabatterAllePerioder != null) {
				children.addAll(getRabatter());
			}
			if (klausuler != null) {
				children.addAll(getKlausuler());
			}
			if (tilAfgangOplysninger != null) {
				children.addAll(getTilAfgangsOplysninger());
			}
			if (omkFritagelser != null) {
				children.addAll(getOmkFritagelse());
			}
			if (minPraemie != null) {
				children.addAll(getMinPraemie());
			}
			if (provModtagerSaetHolder != null) {
				children.addAll(provModtagerSaetHolder.getBoListe());
			}
			if (aftaleAfregningEjSamles != null) {
				children.addAll(getAftaleAfregningEjSamles());
			}
			if (aftaleOmraadeRisikosted != null) {
				children.addAll(getAftaleOmraadeRisikosted());
			}
			if (aftalePBSDebitorGruppeNr != null) {
				children.addAll(getAftalePBSDebitorGruppeNr());
			}

		}
		return children;
	}

	public List<PbsDebitorgruppeBO> getAftalePBSDebitorGruppeNr() {
		if (aftalePBSDebitorGruppeNr == null) {
			loadAftalePBSDebitorGruppeNr();
		}
		return aftalePBSDebitorGruppeNr;
	}

	public PBSTilmeldingBO getPBSTilmelding() {
		if (pbsTilmelding == null) {
			loadPBSTilmelding();
		}
		return pbsTilmelding;
	}

	public List<RisikostedBO> getAftaleOmraadeRisikosted() {
		if (aftaleOmraadeRisikosted == null) {
			loadAftaleOmraadeRisikosted();
		}
		return aftaleOmraadeRisikosted;
	}

	public List getChildrenAllChildrenSelected() {
		List children = getChildren();
		List c = null;
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (((BusinessObject) children.get(i)).isSelecteret()) {
					c = ((BusinessObject) children.get(i)).getChildren();
					if (c != null)
						children.addAll(c);
				}
			}
		}
		return children;
	}

	public void printChildrenSelected() {
		List c = getChildrenAllChildrenSelected();
		for (Iterator iter = c.iterator(); iter.hasNext(); ) {
			BusinessObject element = (BusinessObject) iter.next();
			if (element.isSelecteret()) {
				log_.info("[child] " + element.getClass().getName() + ": " + element.toString());
			}

		}
	}

	public void clearPersistens() {
		super.clearPersistens();
		if (aftaleEgenskabSystem != null)
			aftaleEgenskabSystem.clearPersistens();
		if (hovedprodukt_ != null)
			hovedprodukt_.clear();
	}

	public void clearStatusLoaded() {
		status = null;
	}

	public void clearAfregningerLoaded() {
		afregningstyper = null;
	}

	public void clearAdresserLoaded() {
		adresser = null;
		tegnesAfAdresser = null;
	}

	public void clearMinPraemieLoaded() {
		minPraemie = null;
	}

	public void setMindsteFraDato(BigDecimal pDato) {
		super.setMindsteFraDato(pDato);
		if (aftaleEgenskabSystem != null)
			aftaleEgenskabSystem.setMindsteFraDato(pDato);
	}

	/**
	 * @MH_NYIKRAFT Test metode der understøtter flytning af aftale frem og tilbage i tid
	 * En udvidet kopi af setMindsteFraDato(BigDecimal pDato) metoden
	 */
	public void setMindsteFraDatoTest(BigDecimal pOrgFraDato, BigDecimal pNyDato) {
		super.setMindsteFraDatoTest(pOrgFraDato, pNyDato);
		if (aftaleEgenskabSystem != null)
			aftaleEgenskabSystem.setMindsteFraDatoTest(pOrgFraDato, pNyDato);
	}

	public List<AftaleAfregningEjSamlesBO> getAftaleAfregningEjSamles() {
		if (aftaleAfregningEjSamles == null) {
			loadAftaleAfregningEjSamles();
		}
		if (aftaleAfregningEjSamles != null) {
			return aftaleAfregningEjSamles;
		}
		return null;
	}

	public List<RabatBO> getRabatter() {
		if (rabatterAllePerioder == null) {
			loadRabatter();
		}
        List<RabatBO> retur = new ArrayList<>(rabatterAllePerioder.size());
        for (RabatBO rabat : rabatterAllePerioder) {
            if (rabat.isSelecteret() ||
                    (rabat.isValgbar() ))
                retur.add(rabat);
        }
        return retur;
	}

	public void clearTilgangsoplysninger() {
		tilAfgangOplysninger = null;
	}

	public List<TilgangAfgangBO> getTilAfgangsOplysninger() {
		if (tilAfgangOplysninger == null) {
			loadTilAfgangOplysninger();
		}
		if (tilAfgangOplysninger != null) {
			return tilAfgangOplysninger;
		}
		return null;
	}

	public TilgangAfgangBO getTilgangAfgangBO(OpsigelseModelObjekt pEntitet) {
		List<TilgangAfgangBO> all = getTilAfgangsOplysninger();
		for (int i = 0; all != null && i < all.size(); i++) {
			if (all.get(i).getEntitet() != null && all.get(i).getEntitet().equals(pEntitet)) {
				return all.get(i);
			}

		}
		return null;
	}

	/**
	 * giver det valgte BO
	 */
	public TilgangAfgangBO getTilgangAfgangBOSelected() {
		List<TilgangAfgangBO> all = getTilAfgangsOplysninger();
		for (int i = 0; all != null && i < all.size(); i++) {
			if (all.get(i).isSelecteret()) {
				return all.get(i);
			}

		}
		return null;
	}

	/**
	 * Som så meget andet vedr. til- og afgang, er det en hackerløsning på at få fat på een bestemt opsigelse.
	 * Væsentligste resultat er udover return-objektet instansvaiablen via setopsigelseTilBrugVedOphoer
	 *
	 * @return det bo der må høre til dette ophør, null hvis ingen
	 */
	public TilgangAfgangBO getTilgangAfgangBOTilDetteOphoer() {
		if (!getAftale().isOphUdfyldt())
			return null;
		final Opsigelse[] fremtidigeOpsigelser = getAftale().getFremtidigeOpsigelser(getDato());

		Opsigelse opsigelseFundet = null;
		if (fremtidigeOpsigelser != null) {
			BigDecimal imorgen = Datobehandling.datoPlusMinusAntal(getDato(), 1);
			for (Opsigelse ops : fremtidigeOpsigelser) {
				if (ops.getOpsigelsesdato().compareTo(imorgen) == 0) {
					opsigelseFundet = ops;
					break;
				}
			}
		}
		if (opsigelseFundet == null)
			return null;

		List<TilgangAfgangBO> all = getTilAfgangsOplysninger();
		for (int i = 0; all != null && i < all.size(); i++) {
			if (all.get(i).getType().equals(opsigelseFundet.getOpsigelsestype())) {
				all.get(i).setopsigelseTilBrugVedOphoer(opsigelseFundet);
				return all.get(i);
			}
		}
		return null;
	}

	public List<OmkFritagelseBO> getOmkFritagelse() {
		if (omkFritagelser == null) {
			loadOmkFritagelse();
		}
		if (omkFritagelser != null) {
			return omkFritagelser;
		}
		return null;
	}

	public List<MinPraemieBO> getMinPraemie() {
		if (minPraemie == null) {
			loadMinPraemie();
		}
		if (minPraemie != null) {
			return minPraemie;
		}
		return null;
	}

	public List<AfregningstypeBO> getAfregningstyper() {
		if (afregningstyper == null) {
			loadAfregningstyper();
		}
		if (afregningstyper != null) {
			return afregningstyper;
		}
		return null;
	}

	/**
	 * @return En liste over de emne og genstande der har/kan have tilknyttet dækninger.
	 * Listen indeholder GenstandBOér
	 */
	public List<GenstandBO> getGenstande() {
		if (genstande == null)
			loadGenstande();
		return genstande;
	}

	/**
	 * @return antal selekterede GenstandBO'er
	 */
	public int countGenstandeSelecterede() {
		List<GenstandBO> gn = this.getGenstande();
		int c = 0;
		if (gn != null && !gn.isEmpty()) {
			for (GenstandBO genstandBO : gn) {
				if (genstandBO.isSelecteret())
					c++;
			}
		}
		return c;
	}

	/**
	 * @return selekterede GenstandBO'er
	 */
	public List<GenstandBO> getGenstandeSelekteret() {
		List<GenstandBO> gn = this.getGenstande();
//		int c = 0;
		if (gn != null && !gn.isEmpty()) {
			List<GenstandBO> retur = new ArrayList<GenstandBO>(gn.size());
			for (GenstandBO genstandBO : gn) {
				if (genstandBO.isSelecteret())
					retur.add(genstandBO);
			}
			return retur;
		}
		return null;
	}

    /**
     *
     * @param kortbnv genstandstype
     * @return selekteret bo med den givne korte bnv, null hvis ingen
     */
    public GenstandBO getGenstandIfSelected(String kortbnv) {
        final List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
        if (genstandeSelekteret != null){
            for (GenstandBO genstand : genstandeSelekteret) {
                if (genstand.getType().hasKortBenaevnelse(kortbnv)){
                    return genstand;
                }
            }
        }
        return null;
    }

    /**
	 * Loader kun oprettede genstande og ikke nye tilladte typer.
	 */
	public List<GenstandBO> getGenstandeGaeldendeOgFremtidige() {
		return getGaeldendeOgFremtidigeGenstande();
	}

	/**
	 * Loader kun oprettede genstande og ikke nye tilladte typer.
	 */
	public List<GenstandBO> getGaeldendeOgFremtidigeGenstande() {
		setMedtagFremtidige(true);
		loadGenstande();
		for (int i = 0; genstande != null && i < genstande.size(); i++) {
			if (!((BusinessObject) genstande.get(i)).isSelecteret()) {
				genstande.remove(i);
				i--;
			}
		}
		setMedtagFremtidige(false);
		return genstande;
	}

	/**
	 * Loader gældende og fremtidige genstande incl nye tilladte typer.
	 */
	public List<GenstandBO> getGaeldendeOgFremtidigeGenstandeInclNye() {
		setMedtagFremtidige(true);
		loadGenstande();
		setMedtagFremtidige(false);
		return genstande;
	}

	/**
	 * Denne liste bliver opbygget på samme tid som listen over genstande.
	 *
	 * @return En liste over emne der kun må optræde een gang på en aftale.
	 * D.v.s. en liste af emner der har/kan have tilknyttet genstande.
	 */
	public List<FaellesGenstandOplBO> getFaellesGenstandOplysninger() {
		if (genstande == null)
			loadGenstande();
		return faellesGenstandOpl;

	}

	/**
	 * Returnerer antallet af tvunget genstande på aftalen.
	 */
	public int getAntalTvungetGenstande() {
		List<GenstandBO> genstande = getGenstande();
		List<GenstandBO> typer = new ArrayList<GenstandBO>();
		int antal = 0;
		if (genstande != null) {
			for (int i = 0; i < genstande.size(); i++) {
				if (!typer.contains((genstande.get(i)))) {
					typer.add(genstande.get(i));
					if ((genstande.get(i)).isTvunget()) {
						antal++;
					}
				}
			}
		}
		return antal;
	}

	/**
	 * Returnerer antallet af emner der er tilknyttet aftalen.
	 */
	public int getAntalEmner() {
		List emner = new ArrayList();
		List faellesGenstandsOpl = getFaellesGenstandOplysninger();
		for (int i = 0; faellesGenstandsOpl != null && i < faellesGenstandsOpl.size(); i++) {
			if (!emner.contains(((BusinessObject) faellesGenstandsOpl.get(i)).getType()))
				emner.add(((BusinessObject) faellesGenstandsOpl.get(i)).getType());
		}
		List genstande = getGenstande();
		for (int i = 0; genstande != null && i < genstande.size(); i++) {
			if (!((GenstandBO) genstande.get(i)).isGenstand() && !emner.contains(((GenstandBO) genstande.get(i)).getType()))
				emner.add(((GenstandBO) genstande.get(i)).getType());
		}
		return emner.size();
	}

	/**
	 * Få aftales aktuelle status.
	 */
	public StatusBO getCurrentStatus() {
		if (status == null) {
			loadStatus();
		}

		for (int i = 0; status != null && i < status.length; i++) {
			if (status[i].isSelecteret()) {
				//log_.info("getCurrentStatus: " + status[i].aftaleStatustype.getStatustype().getBenaevnelse());
				return status[i];
			}
		}
		return null;
	}

	protected void loadMinPraemie() {

		minPraemie = new ArrayList<MinPraemieBO>();
		AftaleMinPrae[] fremtidigeMinPrae = null;
		AftpMinPrae minPraeType = null;
		AftaleMinPrae minPrae = null;
		boolean fremtidige = false;

		if (!isNew()) {
			minPrae = ((Aftale) getEntitet()).getAftaleMinPraeMedHoejesteBeloeb(getDato());
			fremtidigeMinPrae = ((Aftale) getEntitet()).getFremtidigeAftaleMinPrae(getDato());
			if (fremtidigeMinPrae != null && fremtidigeMinPrae.length > 0)
				fremtidige = true;
		}

		// Find de lovlige AftpMinPrae, og find den med det største beløb, og anvend denne.
		if (((Aftaletype) getType()).getAftpMinPrae(getDato()) != null) { //ass13795 - nu kaldes med BO-dato
			minPraeType = ((Aftaletype) getType()).getAftpMinPraeMedHoejesteBeloeb(getDato());
		}

		// Der er fundet et brugerbestemt minimumspræmie, opret BO med entiteten OG typen fra regelsæt
		if (minPrae != null) {
			minPraemie.add(new MinPraemieBO(minPrae, this, getDato(), fremtidigeMinPrae, minPraeType));
		}
		// ... ellers bare opret BO med typen fra regelsæt
		else if (minPraeType != null) {
			minPraemie.add(new MinPraemieBO(minPraeType, this, getDato(), fremtidigeMinPrae));
		}
		if (medtagFremtidige()) {
			for (int i = 0; fremtidigeMinPrae != null && i < fremtidigeMinPrae.length; i++) {
				minPraemie.add(new MinPraemieBO(fremtidigeMinPrae[i], this, getDato(), fremtidigeMinPrae, fremtidigeMinPrae[i].getAftaletype().getAftpMinPraeMedHoejesteBeloeb(fremtidigeMinPrae[i].getGld())));
			}
		}
	}

	protected void loadAftaleAfregningEjSamles() {
		aftaleAfregningEjSamles = new ArrayList<AftaleAfregningEjSamlesBO>();
		// Hvis aftalen er ny..
		if (isNew()) {
			aftaleAfregningEjSamles.add(new AftaleAfregningEjSamlesBO(null, this, getDato(), null));
		} else {
			// Finde evt. eksisterende
			AftaleAfregningEjSamles[] alleEjSamles = ((Aftale) getEntitet()).getAftaleAfregningEjSamles();
			if (alleEjSamles != null && alleEjSamles.length > 0) {
				AftaleAfregningEjSamles[] fremtidige = (AftaleAfregningEjSamles[]) Datobehandling.findFremtidige(alleEjSamles, getDato());

				AftaleAfregningEjSamles[] gld = (AftaleAfregningEjSamles[]) Datobehandling.findGaeldende(alleEjSamles, getDato());
				if (gld != null && gld.length > 0) {
					// Der bør kun, og kan kun være én gld. ad gangen!
					aftaleAfregningEjSamles.add(new AftaleAfregningEjSamlesBO(gld[0], this, getDato(), fremtidige));
				} else {
					aftaleAfregningEjSamles.add(new AftaleAfregningEjSamlesBO(null, this, getDato(), fremtidige));
				}
			}
			// Der fandtes ingen eksisterende - opret nu
			else {
				aftaleAfregningEjSamles.add(new AftaleAfregningEjSamlesBO(null, this, getDato(), null));
			}
		}
	}

	protected void loadOmkFritagelse() {
		omkFritagelser = new ArrayList<>();
//		List<Omkostningstype> gyldigeStempelTyper = null;
//		if (((Aftaletype) getType()).getStempelprincipper() != null)
//			gyldigeStempelTyper = new ArrayList(Arrays.asList(((Aftaletype) getType()).getStempelprincipper()));


		if (!isNew()) {
			AftaleOmkFritagelse[] omkFri = ((Aftale) getEntitet()).getAftaleOmkFritagelse();
			if (omkFri != null) {
				// Find den første og bedste omkostningfritagelse og lav et nyt BO
				if (omkFri[0] != null)
					omkFritagelser.add(new OmkFritagelseBO(omkFri[0], this, getDato()));

			}
		}
//		if (gyldigeStempelTyper != null && gyldigeStempelTyper.size() > 0) {
//			for (int g = 0; g < gyldigeStempelTyper.size(); g++) {
//				RegelsaetType type = gyldigeStempelTyper.get(g);
//				OmkFritagelseBO omkFritagelseBO = new OmkFritagelseBO(type, this, getDato());
//				omkFritagelser.add(omkFritagelseBO);
//				if (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering() && !isNew()) {
//					AftaleOmkostningstype aftaleOmkostningstype = getAftale().getAftaleOmkostningstype((Omkostningstype) type);
//					if (aftaleOmkostningstype != null) {
//						omkFritagelseBO.setStempelSaldoToSave(aftaleOmkostningstype.getOmkostningsbeloeb());
//						omkFritagelseBO.setSelecteret(true);
//						omkFritagelseBO.setFritagelsesPct(null);
//						omkFritagelseBO.setIsNew(false);
//					}
//				}
//			}
//		}
	}


	protected void loadTilAfgangOplysninger() {

		tilAfgangOplysninger = new ArrayList<>();

		if (overtagTilAfgangsoplysningerFra_ != null) {
			return;
			// Så flytter vi dem senere fra den gamle aftale
		}

		boolean loadSelOnly = !isNew() && adfaerdStrategy_ != null && (!adfaerdStrategy_.loadBOUselekterede());

		if (!loadSelOnly && !isNew() && isKopi()) {
			// Ved kopiering kopierer vi alle og ikke kun
			Opsigelse[] os = getAftale().getOpsigelseAlle();
			if (os != null) {
				for (Opsigelse opsigelse : os) {
					Opsigelsesoplysning opsigelsesoplysning = opsigelse.getOpsigelsesoplysning();
					if (opsigelsesoplysning != null && opsigelsesoplysning.hasOpsigelsesOplysningExtended()) {
						loadSelOnly = true; // ediopsigelser kan ikke erstattes af ikke-ediopsigelser
					}
				}
			}
		}

		if (!loadSelOnly) {

			// Find alle gyldige opsigelsestyper (i dette tilfælde er ALLE typer tilladt)
			Opsigelsestype[] tps = (Opsigelsestype[]) DBServer.getInstance().getVbsf().get(OpsigelsestypeImpl.class, getDato());
			List<Opsigelsestype> alleGyldigeOpsTyper = ContainerUtil.asList(tps);

			//		List alleGyldigeOpsTyper = new ArrayList( ContainerUtil.asList( ((Aftaletype)getType()).getOpsigelsestype( getDato()) ) );
			Opsigelse[] fremtidigeOpsigelser = null;

			if (aftale != null) {
				fremtidigeOpsigelser = ((Aftale) getEntitet()).getFremtidigeOpsigelser(getDato());
			}
			// Hvis ikke nytegning, Find den opsigelsetype som aftalen er tilknyttet og fjern fra listen over mulige (så det ikke giver dubletter)
			if (!isNew()) {
				Opsigelse os = ((Aftale) getEntitet()).getOpsigelseGld(getDato());
				if (os != null) {
					int y = alleGyldigeOpsTyper.indexOf(os.getOpsigelsestype());
					if (y >= 0) {
						alleGyldigeOpsTyper.remove(y);
					}
					// Tilføj den tilknyttede tilafgangsoplysning til listen + evt. fremtidige
					TilgangAfgangBO tilgangAfgangBO = new TilgangAfgangBO(os, this, getDato(), fremtidigeOpsigelser);
					tilAfgangOplysninger.add(tilgangAfgangBO);
					tilgangAfgangBO.setAllOpsigelserDenneHolder(getAftale().getOpsigelseAlle());
				}
				if (tilOgAfgangeMedtagFremtidige) {
					setMedtagFremtidige(true);
				}
				if (medtagFremtidige()) {
					for (int i = 0; fremtidigeOpsigelser != null && i < fremtidigeOpsigelser.length; i++) {
						tilAfgangOplysninger.add(new TilgangAfgangBO(fremtidigeOpsigelser[i], this, getDato(), fremtidigeOpsigelser));
					}
				}
			}

			// Tilføj de muligt tilafgangsoplysninger til listen + evt. fremtidige
			if (alleGyldigeOpsTyper != null && alleGyldigeOpsTyper.size() > 0) {
				for (int i = 0; i < alleGyldigeOpsTyper.size(); i++) {
					RegelsaetType type = alleGyldigeOpsTyper.get(i);
					tilAfgangOplysninger.add(new TilgangAfgangBO(type, this, getDato(), fremtidigeOpsigelser));
				}
			}
			// Sortér listen efter Opsigelsestype sekvens
			Collections.sort(tilAfgangOplysninger, BusinessObject.TYPE_SEKVENS_COMPARATOR);
		} else {
			Opsigelse[] os = getAftale().getOpsigelseAlle();
			if (os != null) {
				for (Opsigelse opsigelse : os) {
					tilAfgangOplysninger.add(new TilgangAfgangBO(opsigelse, this, getDato(), null));
				}
				tilAfgangOplysninger.get(0).setAllOpsigelserDenneHolder(os);
			}
		}
		if (!loadOpsigelserPersisterede) {
			for (TilgangAfgangBO bo : tilAfgangOplysninger) {
				bo.setInsertUbetinget(true);
			}
		}

	}

	/**
	 * Specialload af til- og afgangsoplysninger, hvor alt persisteret ignoreres
	 *
	 * @param p
	 */
	public void setLoadOpsigelserPersisterede(boolean p) {
		loadOpsigelserPersisterede = p;
	}

	protected void loadRabatter() {

		rabatterAllePerioder = new ArrayList<>();  // Liste der indeholder alle rabatter til aftaletypen + de valgte rabatter på aftalen

		Aftaletype typen = (Aftaletype) getType();
//        BigDecimal regelaflaesningsdato = getTarifdatoSubsidiaertRegelaflaesningsdato();
		RegelsaettypeRelation[] rels = typen.getRabattypeRelation(null);
		if (rels == null)
			return;

		List<RegelsaettypeRelation> alleGyldigeRabattyper = new ArrayList<>(ContainerUtil.asList(rels));

		// Find de 'valgte' rabatter på aftalen
		AftaleRbtp[] valgteRabatter = null;
		AftaleRbtp[] fremtidigeRabatter = null;
		if (getEntitet() != null) {
			if (!medtagFremtidige())
				valgteRabatter = ((Aftale) getEntitet()).getAftaleRabatTyper(getDato());
			else
				valgteRabatter = ((Aftale) getEntitet()).getAftaleRabatTyperGldOgFremtidige(getDato());
			fremtidigeRabatter = ((Aftale) getEntitet()).getFremtidigeRabatter(getDato());
		}

		// Indsæt de 'valgte' rabatter i rabatter listen, og fjern dem fra alleGyldigeRabattyper listen
		for (int i = 0; valgteRabatter != null && i < valgteRabatter.length; i++) {

			int y = alleGyldigeRabattyper.indexOf(new RegelsaettypeRelation(valgteRabatter[i].getRabattype(), false));
			RegelsaettypeRelation helper = null;
			if (y >= 0) {
				helper = alleGyldigeRabattyper.get(y);
				if (adfaerdStrategy_ == null || (adfaerdStrategy_.loadRabatBOTvungne() || !helper.isTvungen())) {
					rabatterAllePerioder.add(new RabatBO(valgteRabatter[i], this, helper, getDato(), fremtidigeRabatter));
				}
				alleGyldigeRabattyper.remove(y);
			} else {
				// Hvis relationen mellem aftalens aftaletype og den tilknyttede rabattype er sat til ophør (reglen sat til ophør)
				if (adfaerdStrategy_ == null || adfaerdStrategy_.loadRabatBOTvungne() || valgteRabatter[i].isFremtidig(getDato())) {
					rabatterAllePerioder.add(new RabatBO(valgteRabatter[i], this, getDato()));
				}
			}
		}

		if (isNew() || adfaerdStrategy_ == null || adfaerdStrategy_.loadRabatBOTvungne()) {
			// Tilføj de resterende gyldige rabatter til rabatter listen
			if (alleGyldigeRabattyper.size() > 0) {
				for (int p = 0; p < alleGyldigeRabattyper.size(); p++) {
					RegelsaettypeRelation regelsaettypeRelation_ = alleGyldigeRabattyper.get(p);
					rabatterAllePerioder.add(new RabatBO(regelsaettypeRelation_.getRegelsaetType(), this, regelsaettypeRelation_, getDato(), fremtidigeRabatter));
				}
			}
		}
	}

	protected void loadAfregningstyper() {
		/**
		 * Ved nyoprettelse sættes en aftales afregningstype altid til "GIROKORT", og denne får markeringen setSelecteret(true)
		 */
		afregningstyper = new ArrayList<AfregningstypeBO>(3);

		RegelsaettypeRelation[] typer = ((Aftaletype) getType()).getAfregningstypeRelation(getDato());

		if (isNew()) { // Nytegning
			String AR_HITS = " GIROKORT ";
			if (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering() ||
					foedesSomPBS_) AR_HITS += " PBS ";
			if (typer != null) {
				for (int i = 0; i < typer.length; i++) {
					Afregningstype afregningstypen = (Afregningstype) typer[i].getRegelsaetType();
					if (AR_HITS.contains(afregningstypen.getKortBenaevnelse().trim())) {
						AfregningstypeBO inst = new AfregningstypeBO(afregningstypen, this, typer[i], getDato(), null);
						if (foedesSomPBS_ && afregningstypen.isPBS()) {
							inst.setSelecteret(true);
						}
						afregningstyper.add(inst);
					}
				}
			}
			if (afregningstyper.size() == 1) {
				// set afregningstypen som 'valgt'
				((BusinessObject) afregningstyper.get(0)).setSelecteret(true);
			}
		} else { // Ændring
			AftaleArtp[] fremtidigeArtp = aftale.getAftaleArtpFremtidige(getDato());
			if (!medtagFremtidige()) {
				AftaleArtp af = aftale.getAftaleArtp(getDato());
				if (af != null) {
					afregningstyper.add(new AfregningstypeBO(af, this, getDato(), fremtidigeArtp));
					if (fremtidigeArtp != null) {
						AftaleArtp[] temp = new AftaleArtp[fremtidigeArtp.length + 1];
						temp[0] = af;
						for (int i = 1; i < fremtidigeArtp.length + 1; i++) {
							temp[i] = fremtidigeArtp[i - 1];
						}
						fremtidigeArtp = temp;

					} else {
						AftaleArtp[] temp = new AftaleArtp[1];
						temp[0] = af;
						fremtidigeArtp = temp;
					}


					// Hvis afregningstypen er GIROKORT - dvs ikke PBS, skal PBS tilføjes så der er mulighed for at vælge den
					if (!af.getAfregningstype().getKortBenaevnelse().trim().equals("PBS")) {

						if (typer != null) {
							for (int i = 0; i < typer.length; i++) {
								if (typer[i].getRegelsaetType().getKortBenaevnelse().trim().equals("PBS")) {
									afregningstyper.add(new AfregningstypeBO(typer[i].getRegelsaetType(), this, typer[i], getDato(), fremtidigeArtp));
								}
							}
						}
					}
					// Hvis afregningstypen er PBS - dvs ikke GIROKORT, skal GIROKORT tilføjes så der er mulighed for at vælge den
					if (!af.getAfregningstype().getKortBenaevnelse().trim().equals(Afregningstype.AFREGNINGSTYPE_GIRO)) {

						if (typer != null) {
							for (int i = 0; i < typer.length; i++) {
								if (typer[i].getRegelsaetType().getKortBenaevnelse().trim().equals(Afregningstype.AFREGNINGSTYPE_GIRO)) {
									afregningstyper.add(new AfregningstypeBO(typer[i].getRegelsaetType(), this, typer[i], getDato(), fremtidigeArtp));
								}
							}
						}
					}
				}
			} else {
				AftaleArtp[] af = aftale.getAftaleArtpGldOgFremtidige(getDato());
				for (int i = 0; af != null && i < af.length; i++) {
					afregningstyper.add(new AfregningstypeBO(af[i], this, getDato(), fremtidigeArtp));
				}
			}
		}
	}

	/**
	 * Denne metode loader to collectioner af BusinessObjecter genstande(GenstandBO) og de fælles
	 * genstandsoplysninger(FaellesGenstandOplBO)
	 */
	protected void loadGenstande() {
		// Load de nye mulige typer og pak dem ind i EmneGenstandTypeWrapper. En genstandstype vil optræde een gang
		// pr. emnetype den er tilknyttet.
		Set<Emnetype> tilladteEmnetyper = new HashSet<>(1);
		ArrayList<EmneGenstandTypeWrapper> emneGenstandsTyper = new ArrayList<EmneGenstandTypeWrapper>();
		RegelsaettypeRelation[] emnetyper = null;
		if ((hovedprodukttype_ != null) && (hovedprodukttype_.length > 0)) {
			for (int k = 0; k < hovedprodukttype_.length; k++) {
				emnetyper = hovedprodukttype_[k].getEmnetypeRelation(getDato());
				if ((emnetyper != null) && (emnetyper.length > 0)) {
					for (int i = 0; i < emnetyper.length; i++) {
						tilladteEmnetyper.add((Emnetype) emnetyper[i].getRegelsaetType());
						RegelsaettypeRelation[] genstandstyper = ((Emnetype) emnetyper[i].getRegelsaetType()).getGenstandstypeRelation(getDato());
						if ((genstandstyper != null) && (genstandstyper.length > 0)) {
							for (int j = 0; j < genstandstyper.length; j++) {
								emneGenstandsTyper.add(new EmneGenstandTypeWrapper(emnetyper[i], genstandstyper[j], hovedprodukttype_[k]));
							}
						} else {
							emneGenstandsTyper.add(new EmneGenstandTypeWrapper(emnetyper[i], hovedprodukttype_[k]));
						}
					}
				}
			}
		}

		// Find ud af om der er genstandstyper under aftalen der er tilknyttet flere emnetyper.
		if (emneGenstandsTyper != null) {
			for (int i = 0; i < emneGenstandsTyper.size(); i++) {
				for (int j = 0; j < emneGenstandsTyper.size(); j++) {
					if ((emneGenstandsTyper.get(i)).isGenstand() &&
							(emneGenstandsTyper.get(i)).isGenstand()) {
						if (((emneGenstandsTyper.get(i)).getGenstandstype().equals(
								(emneGenstandsTyper.get(j)).getGenstandstype())) && (i != j)) {

							(emneGenstandsTyper.get(i)).setFindesGenstandstypePaaAndreEmnetyper(true);
							break;
						}
					}
				}
			}
		}

		// Load de eksisterende genstande
		List<GenstandObjectIF> tilknyttedegenstande = null;
		if (getEntitet() != null)
			if (!medtagFremtidige())
				tilknyttedegenstande = ((Aftale) getEntitet()).getGenstandsObjekterGld(getDato());
			else
				tilknyttedegenstande = ((Aftale) getEntitet()).getGenstandsObjekter(getDato());

		ArrayList<EmneGenstandWrapper> emneGenstande = new ArrayList<EmneGenstandWrapper>();
		if (tilknyttedegenstande != null) {
			for (int i = 0; i < tilknyttedegenstande.size(); i++) {
				if (tilknyttedegenstande.get(i) instanceof Genstand) {
					Genstand genstand = (Genstand) tilknyttedegenstande.get(i);
					Emne emne = genstand.getEmne();
					if (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering()) {
						if (!tilladteEmnetyper.contains(emne.getEmnetype())) {
							emne.setEmnetype(tilladteEmnetyper.iterator().next());
							System.err.println("Emnetype udskiftet til noget en gyldig " + emne.getEmnetype().getBenaevnelse());
						}
					}

					// Find den tilhørende EmneGenstandTypeWrapper

					EmneGenstandTypeWrapper typeWrapper = new EmneGenstandTypeWrapper(
							new RegelsaettypeRelation(emne.getRegelsaetType(), false),
							new RegelsaettypeRelation(genstand.getRegelsaetType(), false),
							((Hovedprodukt) emne.getAftaleParent(emne.getGld())).getHovedprodukttype());

					for (int j = 0; j < emneGenstandsTyper.size(); j++) {
						if (emneGenstandsTyper.get(j).equals(typeWrapper))
							typeWrapper = (EmneGenstandTypeWrapper) emneGenstandsTyper.get(j);
					}
					emneGenstande.add(new EmneGenstandWrapper(emne, genstand, typeWrapper));

				} else {
					Emne emne = (Emne) tilknyttedegenstande.get(i);
					// Find den tilhørende EmneGenstandTypeWrapper
					EmneGenstandTypeWrapper typeWrapper = new EmneGenstandTypeWrapper(
							new RegelsaettypeRelation(emne.getRegelsaetType(), false),
							((Hovedprodukt) emne.getAftaleParent(emne.getGld())).getHovedprodukttype());

					for (int j = 0; j < emneGenstandsTyper.size(); j++) {
						if (emneGenstandsTyper.get(j).equals(typeWrapper))
							typeWrapper = (EmneGenstandTypeWrapper) emneGenstandsTyper.get(j);
					}
					emneGenstande.add(new EmneGenstandWrapper(emne, typeWrapper));
				}
			}
		}


		genstande = new ArrayList<GenstandBO>();
		// Tilføj de eksisterende emne/genstande til listen
		for (int i = 0; i < emneGenstande.size(); i++) {
			EmneGenstandWrapper emneGenstandWrapper = emneGenstande.get(i);
			if (emneGenstandWrapper.isGenstand()) {
				if (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering()) {
					Genstand genstand = emneGenstandWrapper.getGenstand();
					BigDecimal ophBeregnet = genstand.getOphBeregnet();
					if (ophBeregnet.intValue() > 0 && genstand.getGld().compareTo(ophBeregnet) > 0) {
						log_.info("dropper helt reelt annulleret genstand " +
								genstand.getLabel(genstand.getGld()));
						continue;
					}
				}
				genstande.add(new GenstandBO((Emnetype) emneGenstandWrapper.getEmnetype().getRegelsaetType(),
						emneGenstandWrapper.getEmne(),
						emneGenstandWrapper.getGenstandstype(),
						emneGenstandWrapper.getGenstand(),
						emneGenstandWrapper.getEmneGenstandTypeWrapper().findesGenstandstypePaaAndreEmnetyper_,
						this,
						getDato(),
						emneGenstandWrapper.getEmneGenstandTypeWrapper().getHovedprodukttype()));
				isLoadedMedGenstande = true;
			} else
				genstande.add(new GenstandBO(emneGenstandWrapper.getEmnetype(),
						emneGenstandWrapper.getEmne(),
						this,
						getDato(),
						emneGenstandWrapper.getEmneGenstandTypeWrapper().getHovedprodukttype()));
		}
		if (isMedNyeRegler()) {
			// Tilføj de tilladte nye typer til listen
			for (int i = 0; i < emneGenstandsTyper.size(); i++) {
				EmneGenstandTypeWrapper emneGenstandTypeWrapper = emneGenstandsTyper.get(i);

				if (emneGenstandTypeWrapper.isGenstand())
					// Find det emne
					genstande.add(new GenstandBO((Emnetype) emneGenstandTypeWrapper.getEmnetype().getRegelsaetType(),
							emneGenstandTypeWrapper.getEmne(),
							emneGenstandTypeWrapper.getGenstandstype(),
							emneGenstandTypeWrapper.findesGenstandstypePaaAndreEmnetyper_,
							this,
							getDato(),
							emneGenstandTypeWrapper.getHovedprodukttype()));
				else
					genstande.add(new GenstandBO(emneGenstandTypeWrapper.getEmnetype(), this, getDato(), emneGenstandTypeWrapper.getHovedprodukttype()));
			}
		}
		// load de fælles genstandsoplysninger
		ArrayList<Emnetype> brugteEmnetyper = new ArrayList<Emnetype>(1);
		ArrayList<Emne> brugteEmner = new ArrayList<Emne>(1);
		faellesGenstandOpl = new ArrayList<>();

		// Tilføj emne til de eksisterende genstande.
		for (int i = 0; i < emneGenstande.size(); i++) {
			EmneGenstandWrapper emneGenstandWrapper = emneGenstande.get(i);
			if (emneGenstandWrapper.isGenstand()) {
				if (!brugteEmner.contains(emneGenstandWrapper.getEmne())) {
					brugteEmner.add(emneGenstandWrapper.getEmne());
					faellesGenstandOpl.add(new FaellesGenstandOplBO(emneGenstandWrapper.getEmne(),
							emneGenstandWrapper.getEmnetype(),
							this,
							getDato(),
							emneGenstandWrapper.getEmneGenstandTypeWrapper().getHovedprodukttype()));
					if (!brugteEmnetyper.contains(emneGenstandWrapper.getEmnetype().getRegelsaetType()))
						brugteEmnetyper.add((Emnetype) emneGenstandWrapper.getEmnetype().getRegelsaetType());
				}
			}
		}
		// Tilføj de emnetyper der ikke findes allerede
		for (int i = 0; i < emneGenstandsTyper.size(); i++) {
			EmneGenstandTypeWrapper emneGenstandTypeWrapper = emneGenstandsTyper.get(i);
			if (emneGenstandTypeWrapper.isGenstand()) {
				if (emneGenstandTypeWrapper.getEmne() != null && !brugteEmner.contains(emneGenstandTypeWrapper.getEmne())) {
					brugteEmner.add(emneGenstandTypeWrapper.getEmne());
					faellesGenstandOpl.add(new FaellesGenstandOplBO(emneGenstandTypeWrapper.getEmne(),
							emneGenstandTypeWrapper.getEmnetype(),
							this,
							getDato(),
							emneGenstandTypeWrapper.getHovedprodukttype()));
					if (!brugteEmnetyper.contains(emneGenstandTypeWrapper.getEmnetype().getRegelsaetType()))
						brugteEmnetyper.add((Emnetype) emneGenstandTypeWrapper.getEmnetype().getRegelsaetType());
				} else if (!brugteEmnetyper.contains(emneGenstandTypeWrapper.getEmnetype().getRegelsaetType())) {
					brugteEmnetyper.add((Emnetype) emneGenstandTypeWrapper.getEmnetype().getRegelsaetType());
					faellesGenstandOpl.add(new FaellesGenstandOplBO(emneGenstandTypeWrapper.getEmnetype(), this, getDato(), emneGenstandTypeWrapper.getHovedprodukttype()));
				}
			}
		}

		// Link GenstandBO sammen med FaellesGenstandOplBO
		for (int k = 0; k < genstande.size(); k++) {
			if ((genstande.get(k)).isGenstand()) {
				if ((faellesGenstandOpl != null) && (faellesGenstandOpl.size() > 0)) {
					if ((genstande.get(k)).getEmne() != null) {
						for (int i = 0; i < faellesGenstandOpl.size(); i++) {
							FaellesGenstandOplBO genstandOpl = faellesGenstandOpl.get(i);
							if (genstandOpl.getEntitet() != null && genstandOpl.getEntitet().equals((genstande.get(k)).getEmne())) {
								(genstande.get(k)).setFaellesGenstandsOplBO(genstandOpl);
							}
						}
					} else {
						for (int i = 0; i < faellesGenstandOpl.size(); i++) {
							FaellesGenstandOplBO genstandOpl = faellesGenstandOpl.get(i);
							if (genstandOpl.getType().equals((genstande.get(k)).getEmnetype())) {
								(genstande.get(k)).setFaellesGenstandsOplBO(genstandOpl);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Få et AftaleEgenskabsSystem tilhørende denne aftale.
	 * <p>
	 * EgenskabsSystemet bliver initialiseret første gang det skal bruges - og
	 * kun hvis.
	 */
	public AftaleEgenskabSystem getAftaleEgenskabSystem() {
		if (aftaleEgenskabSystem == null) {
			loadAftaleEgenskabSystem();
		}
		return aftaleEgenskabSystem;
	}

	/**
	 * @return getEgenskabSystem
	 */
	@Override
	public EgenskabSystem getEgenskabSystem() {
		return getAftaleEgenskabSystem();
	}

	/**
	 * Udvidet search efter sekundære typer
	 *
	 * @param searchType
	 * @return fundet type eller null hvis ingen
	 */
	@Override
	public RegelsaetType searchExtended(RegelsaetType searchType) {
		if (searchType instanceof Omraadetype) {
			if (omraadetypeNonPersistent != null)
				return omraadetypeNonPersistent;
			Omraadetype ortpthis = getOmraadetype();
			if (ortpthis != null) {
				// understøtter kun persisterede tarifområder
				if (ortpthis.equals(searchType))
					return searchType;
			}
		}
		if (searchType instanceof Totalkundetype) {
			if (getAftale() != null) {
				final AftaleTotalkundetype aftaleTotalkundetype = getAftale().getAftaleTotalkundetype(getDato());
				if (aftaleTotalkundetype != null) {
					// understøtter kun persisterede Totalkundeordninger
					if (aftaleTotalkundetype.getTotalkundetype().equals(searchType))
						return searchType;
				}
			} else {
				// TODO er det et problem ?
			}
		}
		return null;
	}

	/**
	 * @return den persisterede aftales tarifområde
	 */
	public Omraadetype getOmraadetype() {
		if (aftale != null) {
            Omraade o = aftale.getOmraade(getDato());
			if (o != null) {
				Omraadetype omraadetype = o.getOmraadetype();
				return omraadetype;
			}
		}
		return null;
	}

	/**
	 * Genvejsmetode til at sætte postnummer til tarifområde i sitationer hvor det er eneste felt.<br>
	 * Til prisberegninger o.l.
	 *
	 * @param pPostnr
	 * @return true hvis sat, false hvis ikke muligt at sætte
	 */
	public boolean setPostnummerTarifomraade(String pPostnr) {
		List<AdresseBO> adresser = getAdresser();
		if (adresser != null) {
			for (AdresseBO adresseBO : adresser) {
				EgenskabSystem.EgenskabSammenhaenge sammenhaeng = adresseBO.getEgenskabSystem().getEgenskabSammenhaeng(Adregenskabsgruppe.POSTNR.trim());
				if (sammenhaeng != null) {
					sammenhaeng.setIndtastetVaerdi(pPostnr);
					setOmraadetypeNonPersistent(null, adresseBO);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Lige nu en webservice testting
	 */
	private Omraadetype omraadetypeNonPersistent = null;

	/**
	 * Non-persistent implementation af områdeudsøgningen
	 *
	 * @param genstandBO
	 * @param forsikringssted
	 */
	public void setOmraadetypeNonPersistent(GenstandBO genstandBO, AdresseBO forsikringssted) {
		OmraadeUdsoegning inst = OmraadeUdsoegning.instansAfOmraadeUdsoegning();
		omraadetypeNonPersistent = inst.getOmraadetypeTarif(this, forsikringssted);
	}

	@Deprecated
	public void setOmraadetypeNonPersistent(Omraadetype omraadetype) {
		/**
		 * Bør kun anvendes til adhoc-/testformål - brug samme metode der tager genstand- og adresseBo
		 */
		omraadetypeNonPersistent = omraadetype;
	}

	public Omraadetype getOmraadetypeNonPersistent() {
		return omraadetypeNonPersistent;
	}

	/**
	 * Få et AftaleEgenskabSystem tilhørende denne aftale, men reload systemet (dvs. returnere ikke
	 * evt. allerede cached aftaleEgenskabSystem.
	 *
	 * @param pReloadDato Datoen for det nye aftaleegenskabssystem -- hvis null reload pr. BO'ets dato
	 * @return et reloaded AftaleEgenskabSystem.
	 */
	public AftaleEgenskabSystem getAftaleEgenskabSystemReload(BigDecimal pReloadDato) {
		if (pReloadDato == null) {
			pReloadDato = getDato();
		}
		loadAftaleEgenskabSystem(pReloadDato);
		return aftaleEgenskabSystem;
	}

	/**
	 * List med KlausulBO'er tilknyttet aftalen
	 */
	public List<KlausulBO> getKlausuler() {
		if (klausuler == null) {
			loadKlausuler();
		}
		return klausuler;
	}

    public List<KlausulBO> getKlausulerSelekteredeAlleNiveauer() {
        List<KlausulBO> klausulerAlle = getKlausuler().stream()
                .filter(bo -> bo.isSelecteret())
                .sorted(new KlausulSekvensComparator())
                .collect(Collectors.toList());

        List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
        if (genstandeSelekteret != null){
            for (GenstandBO gnbo : genstandeSelekteret) {
                List<KlausulBO> klausulerGn = gnbo.getKlausuler().stream()
                        .filter(bo -> bo.isSelecteret())
                        .sorted(new KlausulSekvensComparator())
                        .collect(Collectors.toList());
                klausulerAlle.addAll(klausulerGn);

                List<DaekningBO> daekninger = gnbo.getDaekningerSelekteredeDaekninger();
                if (daekninger != null){
                    for (DaekningBO daekning : daekninger){
                        List<KlausulBO> klausulerD = daekning.getKlausuler().stream()
                                .filter(bo -> bo.isSelecteret())
                                .sorted(new KlausulSekvensComparator())
                                .collect(Collectors.toList());
                        klausulerAlle.addAll(klausulerD);
                    }
                }
            }
        }
        // todo sikker sortering på samme sekvens som gl. løsning og samme sekvens af typer på samme niveau (gntp dktp)
        return klausulerAlle;
    }

    /**
     *
     * @param pDaekningstype
     * @return true hvis this har en valgt dækning af den angivne type
     */
    public boolean hasDaekningValgt(Produkttype pDaekningstype) {
        final List<DaekningBO> daekningerSelekteredeDaekninger = getDaekningerSelekteredeDaekninger();
        for (DaekningBO daekning : daekningerSelekteredeDaekninger){
            if (daekning.getType().equals(pDaekningstype))
                return true;
        }
        return false;
    }

    class KlausulSekvensComparator implements Comparator<KlausulBO>{

        @Override
        public int compare(KlausulBO o1, KlausulBO o2) {
            return o1.getSortSeq().compareTo(o2.getSortSeq());
        }
    }

    /**
	 * Initialiserer et nyt aftaleEgenskabssystem med aftalens typer og dato.
	 */
	protected void loadAftaleEgenskabSystem() {
		BigDecimal dato = this.alternativLoadDato_ != null ? this.alternativLoadDato_ : getDato();
		loadAftaleEgenskabSystem(dato);

	}

	/**
	 * Initialiserer et nyt aftaleEgenskabssystem med aftalens typer, men med overstyret dato.
	 *
	 * @param pDato
	 */
	private void loadAftaleEgenskabSystem(BigDecimal pDato) {
		aftaleEgenskabSystem = new AftaleEgenskabSystem(aftale, EgenskabFactoryImpl.AFTALE);
		aftaleEgenskabSystem.setMedtagFremtidige(medtagFremtidige());
		Aftaletype[] typer = {(Aftaletype) getType()};
		aftaleEgenskabSystem.setHolder(this);
		aftaleEgenskabSystem.setAktuelDato(pDato); // 25aug10 skal bruges i setMuligeTyper nedenfor
		aftaleEgenskabSystem.setMuligeTyper(typer);
		aftaleEgenskabSystem.setTypeSelecteret((Aftaletype) getType());

		//evt. fortælle egenskabssystem at det er tilbud (men ikke godkendt tilbud) vi er i gang med.
		if (isTilbudAtbehandleSom()) {
			aftaleEgenskabSystem.setTilbud(true);
		}


		if (DEBUG) debug("LOAD aftaleegenskabsystem er aftalen (" + toString()
				+ ") tariferet: " + isTariferet());

	}


	protected void loadKlausuler() {
		klausuler = new ArrayList<KlausulBO>();

		// De betingelsestyper der er lovlige i forhold til aftalestype.
		ArrayList<RegelsaettypeRelation> betingelsestyper = new ArrayList<>(
				ContainerUtil.asList(((Aftaletype) getType()).getBetingelsestype(getDato(), isTilbud() && !isVedAtGodkendeTilbud())));

		// Hvis en entitet findes (aftale) findes de tilknyttede betingelsestyper.
		BttpAftale[] bttpAftale = null;
		BttpAftale[] bttpAftaleFremtidige = null;
		if (getEntitet() != null) {
			if (!medtagFremtidige())
				bttpAftale = ((Aftale) getEntitet()).getBttpAftale(getDato());
			else
				bttpAftale = ((Aftale) getEntitet()).getBttpAftaleGldOgFremtidige(getDato());
			bttpAftaleFremtidige = ((Aftale) getEntitet()).getBttpAftaleFremtidige(getDato());
		}

		boolean loadSelOnly = adfaerdStrategy_ != null && (!adfaerdStrategy_.loadKlausulBOTvungne());

		// De allerede tilknyttede betingelsestyper
		for (int i = 0; bttpAftale != null && i < bttpAftale.length; i++) {
			if (loadSelOnly) {
				KlausulholdertypeKlausultype regelRel = KlausulholdertypeKlausultypeImpl.getKlausultypeXxxxType(bttpAftale[i].getKlausultype(), (Aftaletype) getType());
				boolean isValgbar = regelRel == null || (isTilbud() && !isVedAtGodkendeTilbud() ? regelRel.isValgbarTilbud() : regelRel.isValgbar());
				if (!bttpAftale[i].getKlausultype().isIndividuel() && !isValgbar)
					continue;
				// De er sagen uvedkommende og tynger bare
			}

			RegelsaettypeRelation rr =
					new RegelsaettypeRelation(bttpAftale[i].getKlausultype(), false);

			// Udnyt at RegelsaettypeRelation har en equals funktion der
			// kigger på typen til at finde index
			int index = betingelsestyper.indexOf(rr);

			if (index >= 0) {
				klausuler.add(new KlausulBO(bttpAftale[i], betingelsestyper.get(index), this, getDato(), bttpAftaleFremtidige));
				betingelsestyper.remove(index);
			} else {
				klausuler.add(new KlausulBO(bttpAftale[i], rr, this, getDato(), bttpAftaleFremtidige));
			}
		}

		// De tilladte betingelsestyper der ikke allerede er tilknyttet.
		if (!(loadSelOnly && !isNew())) {
			for (int i = 0; i < betingelsestyper.size(); i++) {
				RegelsaettypeRelation rr = betingelsestyper.get(i);
				klausuler.add(new KlausulBO(rr, this, getDato(), bttpAftaleFremtidige));
			}
		}
	}

	/**
	 * Metoden anvendes i forbindelse med genoptag ophørt forsikring til at tage højde for genstande som er
	 * sat til ophør på samme date som selve aftalen. Aftalens genstande gennemløbes, og
	 * dem som har registreret ophørsdato = aftalens ophørsdato fjernes, så de ikke bliver gemt på den nye
	 * forsikring.
	 * @param pFraAftale
	 */
	public void removeAftaleDaekningerOphoerFraGenoptag(Aftale pFraAftale) {

		BigDecimal dato = BigDecimal.ZERO;

		List<DaekningBO> daekninger = this.getValgteDaekninger();
		// Find evt. genstande som har samme ophørsdato som aftalen, fjern genstanden og alle dens dækninger. Disse
		// skal ikke kopieres med på den nye aftale
		// HOT-651 Undtagelse: Hvis ophørt pga. EDI skal genstanden med alligevel
		boolean skalFjernes = true;
		Opsigelse[] opsigelseTab = pFraAftale.getOpsigelseAlle();
		if (opsigelseTab != null) {
			for (Opsigelse opsigelse : opsigelseTab) {
				if (Datobehandling.datoPlusMinusAntal(opsigelse.getOpsigelsesdato(), -1).compareTo(this.getTilDato()) == 0 && opsigelse.getOprBruger().trim().equals("EDIUSR")) {
					skalFjernes = false;
					break;
				}
			}
		}
		List<GenstandBO> genstande = this.getGenstande();
		if (genstande != null && genstande.size() > 0) {
			for (int i = 0; i < genstande.size(); i++) {

				GenstandBO genstandBO = genstande.get(i);
				if (genstandBO.getTilDato().compareTo(this.getTilDato()) == 0) {
					if (skalFjernes) {
						// Genstand med ophørsdato = aftalens ophørsdato fundet - genstanden skal fjernes fra listen, samt alle dens dækninger
						List<DaekningBO> gnPd = genstandBO.getDaekninger();
						if (gnPd != null && gnPd.size() > 0) {
							for (int k = 0; k < gnPd.size(); k++) {

								if (daekninger.contains((gnPd.get(k)))) {
									daekninger.remove((gnPd.get(k)));
								}
								gnPd.remove(k);
								k--;
							}
						}
						genstande.remove(i);
						i--;
					}
					else {
						genstandBO.setTilDato(dato);
					}
				}
			}
		}

		if (daekninger != null) {
			for (int i = 0; i < daekninger.size(); i++) {
				(daekninger.get(i)).setTilDato(dato);
			}
		}
		this.setTilDato(dato);
	}

	/**
	 * "Overloading" af setOphoer så vi kan se at det skyldes manglende betaling. Til brug for EDI.
	 * Se JavaDoc til setOphoer.
	 */
	public void setOphoerPgaManglendeBetaling(BigDecimal pDato, int pOphoerAnnullerAendre) {
		ophoerSkyldesManglendeBetaling = true;
		setOphoer(pDato, pOphoerAnnullerAendre);
	}

	/**
	 * Metoden håndtere aftalestatusskift i forbindelse med ophør / annullering / fjernelse af ophør. Metoden sørger for
	 * at aftalens nuværende status bliver ændret, og en ny statustype tilknyttes (hvis muligt).
	 * Herefter kaldes setOphoer() metoden på BusinessObejct som håndtere resten af ophør/annullerings
	 * forløbet.
	 *
	 * @see dk.gensam.gaia.business.BusinessObject#setOphoer(BigDecimal, int)
	 */
	public void setOphoer(BigDecimal pDato, int pOphoerAnnullerAendre) {

		ophoerAnnullerAendre_ = pOphoerAnnullerAendre;
		tempNyOphDato = pDato;

		StatusBO[] statusBOer = getStatus();

		if (statusBOer != null) {

			if (ophoerAnnullerAendre_ == GaiaConst.OPHOER || ophoerAnnullerAendre_ == GaiaConst.AENDRE_OPHOER) {

				for (int i = 0; i < statusBOer.length; i++) {
					if (statusBOer[i].isOphoerstype()) {
						// Hvis BO'et ikke er nyt, skal det sættes til ophør. I en ændring at ophør sættes statustypen til ophør,
						// og revideringhåndteringeng sørger for der oprettes en ny statustype til aftalen. Statustyper som sættes
						// til ophør får ALTID de sidste 4 karakterer i revBruger sat til "#OPH"
						if (!statusBOer[i].isNew()) {
							statusBOer[i].setSelecteret(false);
							// Sæt 'alternativ' revBruger sidste 4 karakterer
							((AftaleStatustype) statusBOer[i].getEntitet()).setAltRevBruger(GaiaConst.AENDRING_BRUGER_OPHOER);
						} else
							statusBOer[i].setSelecteret(true);
					} else {
						// Hvis det er en ny korttids aftale skal ny tegnings statustype udlægges.
						if (!(isNew() && isKorttidsAftale())) {

							statusBOer[i].setSelecteret(false);

							if (((AftaleStatustype) statusBOer[i].getEntitet()) != null) {
								// Sæt alternativ revBruger sidste 4 karakterer
								((AftaleStatustype) statusBOer[i].getEntitet()).setAltRevBruger(GaiaConst.AENDRING_BRUGER_OPHOER);
							}
						}
					}
				}
			}

			// Hvis ophør fortrydes, skal 'klar ophør' statustype sættes til ophør, og aftalens status skiftes tilbage til den statustype
			// den havde inden da. Det er AS400 programmet GOPRDGBG (Genopret dagbogsopgaver) som sørger for at genoplive den
			// rigtige status til aftalen.
			else if (ophoerAnnullerAendre_ == GaiaConst.FJERN_OPHOER && statusBOer[0].isOphoerstype()) {

				statusBOer[0].setSelecteret(false);

				if (((AftaleStatustype) statusBOer[0].getEntitet()) != null) {
					// Sæt alternativ revBruger sidste 4 karakterer
					((AftaleStatustype) statusBOer[0].getEntitet()).setAltRevBruger(GaiaConst.AENDRING_BRUGER_OPHOER);
				}
			} else if (ophoerAnnullerAendre_ == GaiaConst.ANNULLERING) {
				for (int i = 0; i < statusBOer.length; i++) {
					if (statusBOer[i].isAnnulleringstype())
						statusBOer[i].setSelecteret(true);
					else
						statusBOer[i].setSelecteret(false);
				}
			}
		}

		super.setOphoer(pDato, ophoerAnnullerAendre_);
	}


	public void setAnnuleret(boolean pAnnulleret) {
		isAnnulleret = pAnnulleret;
	}

	public void setNytTilbud(boolean pNytTilbud) {
		isNytTilbud = pNytTilbud;
	}

	public void setIsForsikringTilTilbud(boolean pIsForsikringTilTilbud) {
		isForsikringTilTilbud_ = pIsForsikringTilTilbud;
	}

	public void setVedAtGodkendeTilbud(boolean pGodkendtTilbud) {
		isVedAtGodkendeTilbud = pGodkendtTilbud;
	}

	/**
	 * Returnere en liste med de børn der skal sættes til ophør, eller de børn
	 * som har børn der skal sættes til ophør. Der vil løbende blive tilføjet elementer
	 * til childrenTilOphoer listen...
	 * <UL>
	 * <LI> Dækninger
	 * <LI> Tilgangs/afgangsoplysninger
	 * </UL>
	 */
	public List getOphoerChildren() {

		List childrenTilOphoer = new ArrayList();
		childrenTilOphoer.addAll(getValgteDaekninger());

		if (ophoerAnnullerAendre_ == GaiaConst.FJERN_OPHOER) {
			if (childrenTilOphoer != null && !childrenTilOphoer.isEmpty()) {
				for (Object bo : childrenTilOphoer) {
					DaekningBO d = (DaekningBO) bo;
					addDaekningTilNyeListe(d);
				}
			}
		}

		/**
		 * Her undersøges om tilgang/afgangsoplysningen skal slettes ved fjern aftale
		 * ophør
		 */
		List<TilgangAfgangBO> tilafgang = getTilAfgangsOplysninger();
		if (tilafgang != null && tilafgang.size() > 0) {
//			BigDecimal aftaleOph = aftale.getOph();
			for (int t = 0; t < tilafgang.size(); t++) {
				TilgangAfgangBO bo = tilafgang.get(t);
				if (bo != null && !bo.isNew()) {
					Opsigelse ops = ((Opsigelse) bo.getEntitet());
					if (ops != null) {
						// TilgangAfgangs oplysningen må kun tilføjes listen hvis den har samme opsigelsesdato som aftalens getOph +1
						if (((Opsigelse) bo.getEntitet()).getOpsigelsesdato().compareTo(Datobehandling.datoPlusMinusAntal(aftale.getOph(), 1)) == 0 && ophoerAnnullerAendre_ == GaiaConst.FJERN_OPHOER)
							childrenTilOphoer.add(tilafgang.get(t));
					}
				}
			}
		}
		return childrenTilOphoer;
	}

	/**
	 * loader nogle sekundære oplysninger on demand - lige nu mest til konvertering
	 */
	public void loadTilKonvertering() {
		if (this.getEntitet() != null) {
			Aftale f = (Aftale) this.getEntitet();
			//AftaleRev
			this.afrev_ = f.getAftaleRev(); // hele historikken incl. før skæringsdato
			if (this.afrev_ != null) {
				for (AftaleRev afrev : afrev_) {
					afrev.getRevidering();
					// Nu har afrevobjektet en direkte reference til sit revideringsobjekt udenom vbsf
				}
			}
			//DagbogAftale
			this.dgaf_ = f.getAlleDagbogAftale(); // hele historikken incl. før skæringsdato
			if (this.dgaf_ != null) {
				for (DagbogAftale dgaf : dgaf_) {
					dgaf.getOpgaveObj();
					// Nu har dgafobjektet en direkte reference til sit opgavesobjekt udenom vbsf
				}
			}

			//Stormflod
			//konverteres senere når alle fordringer er konverteret

			//FiktivStormflod
			this.fstf_ = f.getFiktivStormflod(); // hele historikken incl. før skæringsdato

			//FiktivAftaleOmkostningstype
			this.fafomtp_ = f.getFiktivAftaleOmkostningstype(); // hele historikken incl. før skæringsdato
			if (this.fafomtp_ != null) {
				for (FiktivAftaleOmkostningstype fafomtp : fafomtp_) {
					fafomtp.getOmkostningstype();
					// Nu har FiktivAftaleOmkostningstypeobjektet en direkte reference til sit omkostningstypesobjekt udenom vbsf
				}
			}

			//FiktivAftaleAarsAfgift
			this.fafaaafg_ = f.getFiktivAftaleAarsAfgift(); // hele historikken incl. før skæringsdato
			if (this.fafaaafg_ != null) {
				for (FiktivAftaleAarsAfgift fafaaafg : fafaaafg_) {
					fafaaafg.getOmkostningstype();
					// Nu har FiktivAftaleAarsAfgiftobjektet en direkte reference til sit omkostningstypesobjekt udenom vbsf
					fafaaafg.getAftaletype();
					// Nu har FiktivAftaleAarsAfgiftobjektet en direkte reference til sit aftaletypesobjekt udenom vbsf
				}
			}

			//AftaleOmkFritagelse
			this.afomfrit_ = f.getAftaleOmkFritagelse(); // hele historikken incl. før skæringsdato
			if (this.afomfrit_ != null) {
				for (AftaleOmkFritagelse afomfrit : afomfrit_) {
					afomfrit.getOmkostningstype();
					// Nu har AftaleOmkFritagelsesobjektet en direkte reference til sit omkostningstypesobjekt udenom vbsf
				}
			}

		} else {
			//AftaleRev
			this.afrev_ = null;
			//DagbogAftale
			this.dgaf_ = null;
			//Stormflod
			this.stf_ = null;
			//FiktivStormflod
			this.fstf_ = null;
			//FiktivAftaleOmkostningstype
			this.fafomtp_ = null;
			//FiktivAftaleAarsAfgift
			this.fafaaafg_ = null;
			//AftaleOmkFritagelse
			this.afomfrit_ = null;
		}
	}

	/**
	 * aftalens liste af revideringer
	 */
	private AftaleRev[] afrev_ = null;

	/**
	 * @return aftalens revideringer, med fuld historik
	 */
	public AftaleRev[] getAftaleRev() {
		return this.afrev_;
	}

	/**
	 * aftalens liste af fiktive omkostningstyper
	 */
	private FiktivAftaleOmkostningstype[] fafomtp_ = null;

	/**
	 * @return aftalens fiktive omkostningstyper, med fuld historik
	 */
	public FiktivAftaleOmkostningstype[] getFiktivAftaleOmkostningstype() {
		return this.fafomtp_;
	}

	/**
	 * aftalens liste af omkostningsfritagelser
	 */
	private AftaleOmkFritagelse[] afomfrit_ = null;

	/**
	 * @return aftalens omkostningsfritagelser, med fuld historik
	 */
	public AftaleOmkFritagelse[] getAftaleOmkFritagelse() {
		return this.afomfrit_;
	}

	/**
	 * aftalens liste af fiktive årsafgifter
	 */
	private FiktivAftaleAarsAfgift[] fafaaafg_ = null;

	/**
	 * @return aftalens fiktive årsafgifter, med fuld historik
	 */
	public FiktivAftaleAarsAfgift[] getFiktivAftaleAarsAfgift() {
		return this.fafaaafg_;
	}

	/**
	 * aftalens liste af dagboger
	 */
	private DagbogAftale[] dgaf_ = null;

	/**
	 * @return aftalens dagboger, med fuld historik
	 */
	public DagbogAftale[] getDagbogAftale() {
		return this.dgaf_;
	}

	/**
	 * aftalens liste af stormflod
	 */
	private Stormflod[] stf_ = null;

	/**
	 * @return aftalens stormflod, med fuld historik
	 */
	public Stormflod[] getStormflod() {
		return this.stf_;
	}

	/**
	 * aftalens liste af fiktive stormflod
	 */
	private FiktivStormflod[] fstf_ = null;

	/**
	 * @return aftalens stormflod, med fuld historik
	 */
	public FiktivStormflod[] getFiktivStormflod() {
		return this.fstf_;
	}

	/**
	 * Når funktionen returnere er alle klassens data loadet.
	 */
	public void load() {
		if (!isLoaded()) {
			if (aftaleEgenskabSystem == null) {
				loadAftaleEgenskabSystem();
			}

			if (klausuler == null) {
				loadKlausuler();
			}

			if (status == null) {
				loadStatus();
			}

			if (rabatterAllePerioder == null) {
				loadRabatter();
			}

			if (relationer == null) {
				loadRelationer();
			}

			if (genstande == null) {
				loadGenstande();
			}

			if (ydelser == null) {
				loadYdelser();
			}

			if (afregningstyper == null) {
				loadAfregningstyper();
			}

			if (tilAfgangOplysninger == null) {
				loadTilAfgangOplysninger();
			}

			if (adresser == null) {
				loadAdresser();
			}

			if (forfald == null) {
				loadForfald();
			}

			if (frekvens == null) {
				loadFrekvens();
			}
/*
			if(!maeglerLoaded) {
                loadMaegler();
            }*/

			if (omkFritagelser == null) {
				loadOmkFritagelse();
			}

			if (minPraemie == null) {
				loadMinPraemie();
			}
			if (aftaleAfregningEjSamles == null) {
				loadAftaleAfregningEjSamles();
			}
			if (aftaleOmraadeRisikosted == null) {
				loadAftaleOmraadeRisikosted();
			}
			if (aftalePBSDebitorGruppeNr == null) {
				loadAftalePBSDebitorGruppeNr();
			}
		}
		setIsLoaded(true);
	}

	public Class getRealClass(Class pClass) {
		//Ydelsesrelationer
		if (pClass.isAssignableFrom(AftalekompYdtpIF.class))
			return AftaleYdtpImpl.class;
		if (pClass.isAssignableFrom(AftalekompYdtpAngivelseIF.class))
			return AftaleYdtpAngImpl.class;
		if (pClass.isAssignableFrom(AftalekompYdtpYdVartpIF.class))
			return AfYdtpYdVartpImpl.class;
		if (pClass.isAssignableFrom(AftalekompRabattypeIF.class))
			return AftaleRbtpImpl.class;
		if (pClass.isAssignableFrom(KlausulholderKlausultype.class))
			return BttpAftaleImpl.class;

		// *** Save i AdresseBO ***
		if (pClass.isAssignableFrom(AdresseHolderAdresseIF.class)) {
			return AftaleAdresseImpl.class;
		}

		// *** Save i RelationsBO ***
		if (pClass.isAssignableFrom(RelationsHolderRelationIF.class)) {
			return IndividAftaleImpl.class;
		}

		return super.getRealClass(pClass);

	}

	/**
	 * Skal kaldes før saveAll hvis klienten vil skippe karenstid i Totalkundeordningen.<br>
	 *     Efter kaldet kan resultatet aflæses i resultTotalkundeTilmelding_
	 */
	public void initierTotalkunde() {
		resultTotalkundeTilmelding_ = null;
		if (getSimuleretRabatPct() != 0.00) {
			if (TotalkundetypeRegelManager.isTotalkundeSelskab() && !isUndladTotalkundeKontrol() && getParent() != null) {
				if (isNew() && !(getParent() instanceof PanthaverBO)) {
					Individ forstager = (Individ) getParent().getEntitet();
					TotalkundeKontrolHelper helper = new TotalkundeKontrolHelper();
					try {
						if (forstager == null)
							throw new IllegalStateException("Kan ikke tilmelde null-individ");
						helper.tilmeldTilOrdning(forstager, getDato());
					}   catch (IllegalStateException kanIkkeTilmeldes) {
						resultTotalkundeTilmelding_ = kanIkkeTilmeldes.getMessage();
					}
				}
			}
		}
	}

	/**
	 *
	 * @return evt. fejlmelding fra seneste tilmelding Totalkunde, null hvis ok.
	 */
	public String getResultatTotalkundetilmelding() {
		return resultTotalkundeTilmelding_;
	}

	/**
	 * Overskrivning af super-klassen for at sikre at efterfølgende proces
	 * udføres efter save.
	 * <p>
	 * Efter aftale og children er gemt, sættes ophør for nye kortidsaftaler og
	 * til sidst kaldes område konsekvenserne.
	 */
	public void saveAll() {
		boolean nyAftale = (getEntitet() == null);
//		boolean retAftalePrIkraft = isAenderingPrIkraft();
		boolean forsikringsaftale = !(getParent() instanceof PanthaverBO);

		boolean huskstatus = statusLoadEnabled;
		statusLoadEnabled = false;
		if (ydelsesBeregningEnabled) {
			beregnAlleBeregnedeYdelserUnderAftalen();
		}
		statusLoadEnabled = huskstatus;

		super.saveAll();
		removeRabatter();

		if (saveFremtidigeAdresser && this.fremtidigeLovligeAdresser != null){
			saveFremtidigeAdresser();
		}

		udfoerTotalkundekontrol();

		if (forsikringsaftale && nyAftale) {
			//Genoptagelse og ny ikrafttrædelse må ikke opfattes som ny oprettelse.
			String aftalehaendelsestypeTilSave = getAftalehaendelsestypeTilSave();
			if (aftalehaendelsestypeTilSave != null && !aftalehaendelsestypeTilSave.equals(Aftalehaendelse.HAENDELSESTYPE_NYIKRAFT)
					&& !aftalehaendelsestypeTilSave.equals(Aftalehaendelse.HAENDELSESTYPE_GENOPTAG_FORSIKRING)) {
				saveSpecielOprettelsesGebyr();
			}
		}

		// TilDato restores
		if (isKorttidsAftale()) {
			setTilDato(tempTilDato);
		}

		// Performance trick! Hvis der ikke adresser eller genstande er loadet,
		// skal området med sikkerhed ikke skiftes.
		boolean dirtyOmr = adresser != null || tegnesAfAdresser != null
				|| genstande != null || faellesGenstandOpl != null || (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering());

		if (isUndladOmraadeKonsekvensVedSaveAll() || this.isUdfoerIndexRegulering()) {
			// Gør ingenting
		} else {
			if ((dirtyOmr && !(getParent() instanceof PanthaverBO) && ophoerAnnullerAendre_ != GaiaConst.ANNULLERING)) {
                BigDecimal tarifDatoFraFelt = getTarifDatoFraFelt();
                if (tarifDatoFraFelt == null)
                    tarifDatoFraFelt = dato_;

                OmraadeKonsekvens.instansAfOmraadeKonsekvens().omraadeKonsekvens(this, tarifDatoFraFelt);
			}
		}

		if (PersistensService.getRevideringsBufferCurrent().isRevideringEnabled() &&
                !undladUdvalgteRevideringer) {
			if (forsikringsaftale && !(isKorttidsAftale())) {
                haandterSkiftFrekvensOgHovedforfald();
            }
		}

		if (forsikringsaftale && EDIHelper.isSelskabTilsluttetEdiPanthaver()) {
			if (isNytTilbud() || isForlaengKorttid)
				disableEdiKommunikation();
			if (getAftale() != null && getAftale().isTilbud())
				disableEdiKommunikation();

			if (ediKommunikationEnabled_) {
				/**
				 * Så giver vi Edi-panthaverløsningen en mulighed for at sniffe til hændelserne<br>
				 */
				BigDecimal igaar = Datobehandling.datoPlusMinusAntal(getDato(), -1);
				boolean ophSatHer = getEntitet() != null && getEntitet().getOph().compareTo(igaar) == 0;
				if (!ophSatHer) {
					EdiPanthaverService.getInstance().sendDaekningOphoert(getOphoerteDaekningerSlettet(), ophoerSkyldesManglendeBetaling, null);
				} /* else
				* #16886 hvis dækningerne er slettet i forb. med forsikringsophør, skipper vi lige ovenstående, da det skal ende ud med
				* forsikringsophør.
				* Var meget i tvivl om tjekket burde ligge i service-metoden, men fravalgte det, da den metode i nogle tilfælde skal
				* acceptere afsendelse af dækningsophør trods forsikringsophør
				*/
			}
			daekningerRegNye = null;
		}
		/**
		 */
		if (overtagTilAfgangsoplysningerFra_ != null) {
			overtagTilAfgangsoplysninger(overtagTilAfgangsoplysningerFra_);
		}

		slettedeDeklarationsRelationerThisAendring_ = null;
		/**
		 *
		 */
		if (ydelserBOBetingede != null && !ydelserBOBetingede.isEmpty()) {
			saveYdelserBetingede(ydelserBOBetingede);
			if (DEBUG) debug("AftaleBO.saveAll : har savet betingede ydelser ");
		} else if (DEBUG) debug("AftaleBO.saveAll : der var ingen betingede ydelser");
	}

    /**
     * Håndterer skift af frekvens og hovedforfald afhængig af aftalens tariferingsstatus
     */
    private void haandterSkiftFrekvensOgHovedforfald() {
        // todo find bedre placering for denne
        DBServer.getInstance().getVbsf().getDagbogAftalePersistensService(getAftale()).setFornyelsesdato();
        if (isTariferet()) {
            if (adfaerdStrategy_ == null || !adfaerdStrategy_.isGsproGsproKonvertering()) {
                if (isHForfaldSkift()) {
                    hfForfaldSkiftBO.skiftHovedforfald();
                    if (!isFrekvensSkift()) {
                        if (dftFrekvensBO == null) {
                            loadFrekvens();
                        }
                        if (dftFrekvensBO != null) {
                            dftFrekvensBO.skiftFrekvens(hfForfaldSkiftBO);
                        }
                    }
                }
                if (isFrekvensSkift()) {
                    frekvensSkiftBO.skiftFrekvens(hfForfaldSkiftBO);
                }
            }
        } else {
            // Udføres kun pr.tegningsdato
            if (getAftale().getGld().compareTo(getDato()) == 0) {
                if (!isNew() && isHovedforfaldSelectionChanged()) {
                    hfForfaldSkiftBO.skiftHovedforfald();
                }
                if (isFrekvensSkift()) {
                    frekvensSkiftBO.skiftFrekvens(null);
                } else {
                    if (dftFrekvensBO == null) {
                        loadFrekvens();
                    }
                    if (dftFrekvensBO != null) {
                        dftFrekvensBO.skiftFrekvens(null);
                    }
                }
            }
        }
    }

    /**
     * Oprydningsmetode, som kan fjernes en gang i 2018 når alle selskaber er på ny præmieregistrering og
     * vi ikke har flere datafejl i afrbtp og pdrbtp
     */
    private void removeRabatter() {
        List<RabatBO> rabatter = getRabatter();
        List<RabatBO> rabatterAllePerioder = this.rabatterAllePerioder;
        if (rabatterAllePerioder != null){
            for (RabatBO rabatBO : rabatterAllePerioder){
                if (!rabatBO.isSelecteret() &&
                        !rabatter.contains(rabatBO)){
                    rabatBO.saveAftalekompRbtp();
                }
            }
        }
        List<DaekningBO> daekningerSelekteredeDaekninger = getDaekningerSelekteredeDaekninger();
        if (daekningerSelekteredeDaekninger != null){
            for (DaekningBO daekningBO : daekningerSelekteredeDaekninger){
                daekningBO.removeRabatter();
            }
        }
    }

    /**
	 * Udfører totalkundekontrol hvis forudsætningerne herfor er opfyldt
	 *
	 */
	public void udfoerTotalkundekontrol() {
		boolean nyAftale = (getEntitet() == null);
		if (TotalkundetypeRegelManager.isTotalkundeSelskab() && !isUndladTotalkundeKontrol() && getParent() != null) {
			if ((nyAftale ||
					isAenderingPrIkraft() ||
					TotalkundetypeRegelManager.isDiplomOrdning())
					&& !(getParent() instanceof PanthaverBO)) {
				BigDecimal anvendDato = dato_.equals(GaiaConst.NULBD) ? totalkundeKontrolDatoVedFjernOphoer : dato_;
				TotalkundeKontrolForstagerImpl inst = TotalkundeKontrolForstagerImpl.createInstance(
						(Individ) getParent().getEntitet(), anvendDato);
				inst.setVognskifte(vognskiftIgangForTotalkunde, (Aftale) getEntitet());
				inst.setTilbudInclusive(((Aftale) getEntitet()).isTilbud());
				if (TotalkundetypeRegelManager.isEtuOrdning()) {
					if (ophoerAnnullerAendre_ == GaiaConst.ANNULLERING) {
						inst.setAftaleAnnulleres((Aftale) getEntitet());
					}
				}
				inst.handleAftaleSave((Aftale) getEntitet(), anvendDato);
			}
		}
	}

	protected boolean isAenderingPrIkraft() {
		Aftale aftale = (Aftale) getEntitet();
		if (aftale != null && getDato() != null) {
			return aftale.getGld().compareTo(getDato()) == 0;
		}
		return false;
	}

	private void overtagTilAfgangsoplysninger(Aftale overtagTilAfgangsoplysningerFra) {
		OQuery qry = new OQuery(AftaleOpsigelseImpl.class);
		qry.add(overtagTilAfgangsoplysningerFra.getId(), "Aftale");
		AftaleOpsigelseImpl[] afoss = (AftaleOpsigelseImpl[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (afoss != null) {
			for (AftaleOpsigelseImpl afos : afoss) {
				Opsigelse opsigelse = afos.getOpsigelse();
				opsigelse.setAftale(getAftale());

				// Fra-opsigelser med gld=aftale.gld omdateres til ny aftale.gld uanset om datoen rykkes frem og tilbage
				// Fra-opsigelser der bare ligger før ny aftale.gld omdateres til ny aftale.gld for ikke at ligge udenfor dækningsperiode
				if (opsigelse.getGld().compareTo(getAftale().getGld()) < 0 ||
						opsigelse.getGld().compareTo(overtagTilAfgangsoplysningerFra.getGld()) == 0) {
					opsigelse.setGld(getAftale().getGld());
				}
				PersistensService.gem(opsigelse);
			}
			DBServer.getInstance().getVbsf().discardAll(AftaleOpsigelseImpl.class);
			DBServer.getInstance().getVbsf().markCollectionDirty((AftaleImpl) this.getAftale(), "Opsigelse");
		}
	}

	/**
	 * Skal kaldes af de systemfunktioner der ved, at der ikke må kommunikeres vedr. panthaverdeklarationer i den pågældende situation.<br>
	 * Altså kun relevant når this ikke selv kan afgøre det<br>
	 * P.t. kendt anvendelse: Ny ikraft, forlængelse af korttid, flyt forsikring incl. pant<br>
	 */
	public void disableEdiKommunikation() {
		ediKommunikationEnabled_ = false;
	}

	protected boolean isEdiKommunikationDisabled() {
		return ediKommunikationEnabled_;
	}

	public void save() {
		boolean isNyOphor = false;

		if (isSelecteret()) {
			if (getEntitet() == null) {
				aftale = PersistensService.opret(AftaleImpl.class);
				overfoerAttributter(aftale);
				aftale.setGld(getFraDato());
				aftale.setOph(getTilDato());
				aftale.setAftaletype((AftaletypeImpl) getType());
				aftale.setTegnesAfIndivid((IndividImpl) getIndivid().getEntitet());
				if (getParent() instanceof PanthaverBO)
					aftale.setTilbydesAfIndivid((IndividImpl) ((PanthaverBO) getParent()).getForsikringstager());
				else
					aftale.setTilbydesAfIndivid(DBServer.getInstance().getSelskab());
				((AftaleImpl) aftale).setAftaleKategoriWhenNew(isNytTilbud() ? 1 : 2);
				PersistensService.save(aftale);

				// Ved første lookup af en AftaleImpl efter aftalen er oprettet,
				// laver VBSF en ny instans af AftaleImpl.
				// Så derfor dette lille hack for at BusinessObjecter og VBSF arbejder på det samme object.
				// 20.08.2002 pk Ja, men ikke længere i Vbsf3
//                if (!DBFactory.vbsf3_)
//                    aftale = (Aftale)DBFactory.lookup(AftaleImpl.class, aftale.getId());

				addToCollection(aftale);
				setEntitet(aftale);
				saveAftalehaendelse();
				//				AftalesAftpImpl aftaleAftp = (AftalesAftpImpl)DBFactory.opret(AftalesAftpImpl.class);

				//				aftaleAftp.setAftale((AftaleImpl)aftale);
				//				aftaleAftp.setAftaletype((AftaletypeImpl)getType());
				//				DBFactory.save(aftaleAftp);
				//				addToCollection(aftaleAftp);

				if (aftaleEgenskabSystem != null) {
					aftaleEgenskabSystem.setOpretMode(true);
					aftaleEgenskabSystem.setEgenskabHolder((EgenskabHolder) getEntitet());
				}
				/**
				 * Panthaverdeklaration?
				 * Så er this et PanthaverdeklarationsBO og gui'en har forbrugt en ventende eller manuel deklaration
				 * Vi sender accept og tilknytter accepten og os selv
				 */
				if (this.ediPanthaverdeklarationDb_ != null) {
					/**
					 * Først sammenknytter vi deklarationerne
					 */
					EDIPanthaverDokument pPanthaverDok = ediPanthaverdeklarationDb_.getAsDocument();
					Panthaverdeklaration phd = EdiPanthaverService.getInstance().getPanthaverdeklaration(aftale);
					Panthaver ph = EdiPanthaverService.getInstance().getPanthaver((Individ) this.getParent().getEntitet());
					phd.setPanthaver(ph);
					EdiPanthaverService.getInstance().tilknyt(ediPanthaverdeklarationDb_, phd);
					EdiPanthaverService.getInstance().afslutManuelOpgave(ediPanthaverdeklarationDb_);
					/**
					 * så sender vi en accept og tilknytter også den til deklarationen
					 */
					EdiPanthaverService.getInstance().sendAcceptTilknyt(pPanthaverDok, this, ediDeklarationstype_E_acceptkoder);
				}
			} else {
				isNyOphor = aftale.getOph().compareTo(getTilDato()) != 0;

				if (isGemEksisterendeAftaleEntitet() ||
						(!isGemEksisterendeAftaleEntitet() &&
								(aftale.getGld().compareTo(getFraDato()) != 0 ||
										aftale.getOph().compareTo(getTilDato()) != 0))) {
					aftale.setGld(getFraDato());
					aftale.setOph(getTilDato());
					    PersistensService.save(aftale);
				}
				saveAftalehaendelse();

				if (vognskiftIgang_) {
					saveSpecielOprettelsesGebyr();
				}
//                }
			}
			if ((hovedprodukttype_ != null) && (hovedprodukttype_.length > 0)) {
				for (int i = 0; i < hovedprodukttype_.length; i++) {
					boolean hovedproduktFundet = false;
					if ((hovedprodukt_ != null) && (hovedprodukt_.size() > 0)) {
						for (int j = 0; j < hovedprodukt_.size(); j++) {
							if ((hovedprodukt_.get(j)).getHovedprodukttype().equals(hovedprodukttype_[i])) {
								hovedproduktFundet = true;
								break;
							}
						}
					}
					if (!hovedproduktFundet) {
						Hovedprodukt hovedprodukt = PersistensService.opret(HovedproduktImpl.class);
						hovedprodukt.setAftale((AftaleImpl) getEntitet());
						hovedprodukt.setHovedprodukttype(hovedprodukttype_[i]);
						hovedprodukt.setGld(getEntitet().getGld());
						hovedprodukt.setOph(BigDecimal.ZERO);
						PersistensService.save(hovedprodukt);
						addToCollection(hovedprodukt);
						hovedprodukt_.add(hovedprodukt);
					}
				}
			}

			// @Hack Fjern ophørsdatoen midlertidigt
			if (aftaleEgenskabSystem != null
					&& aftaleEgenskabSystem.isKorttidAftale()) {

				tempTilDato = getTilDato();
				aftale.setOph(BigDecimal.ZERO);
			}

			// OBS FEFA - gem af egenskab ved fjern ophør (valg af sagsbehandler ved ophør dialog)
			if (aftaleEgenskabSystem != null) {
				handleDeklarationsnummerAendringPanthaverdeklaration(aftaleEgenskabSystem);
				aftaleEgenskabSystem.gem();
				handleOmprioriteringPanthaverdeklaration(aftaleEgenskabSystem);
			}

			// @Hack Restore ophørsdato
			if (aftaleEgenskabSystem != null
					&& aftaleEgenskabSystem.isKorttidAftale()) {

				aftale.setOph(tempTilDato);
			}

			if (getParent() instanceof PanthaverBO)
				((PanthaverBO) getParent()).saveInGnAf();
		} else if (!isSelecteret() && !isNew() && getDato() != null) {
			if (getEntitet().getGld().compareTo(getDato()) <= 0) {
				Aftale aftale_ = (Aftale) getEntitet();
				if (aftale_ != null) {

					if (ophoerAnnullerAendre_ == GaiaConst.ANNULLERING) { // && aftale_.getDaekningerGldOgFremtidige(pDato) == null || ger().size()==0)){

						// Hvis aftalen ikke har nogle dækninger der er tariferet, skal der oprettes en record i AfPdAnnullering
						// HUSK!!! Hvis der findes tariferet dækninger på aftalen, er det dækningerner som skal oprettes i AfPdAnnullering,
						// og IKKE aftalen.
						// Check om der findes tariferet dækninger på aftalen
						Produkt[] daekninger = (Produkt[]) Datobehandling.findGaeldendeOgFremtidige(aftale_.getDaekninger(), getDato());
						boolean tariferetDaekningFundet = false;
						for (int i = 0; daekninger != null && i < daekninger.length; i++) {
							if (daekninger[i].isTariferet()) {
								tariferetDaekningFundet = true;
								break;
							}
						}


						if (!tariferetDaekningFundet) {
							// Ingen dækninger fundet - opret aftalen i AfPdAnnullering
							AfPdAnnullering annullering = (AfPdAnnullering) PersistensService.opret(AfPdAnnulleringImpl.class);
							annullering.setAftale(aftale_);
							// Sørger for at ProduktID tildeles 2 'havelåger' ##
							annullering.setProduktEmptyID();
							annullering.setAfgaeldendefra(aftale_.getGld());
							annullering.setUdfoertmarkeringj_n(false);
							annullering.setAftaleannulleretj_n(true);

							PersistensService.save(annullering);
						}

						// Opret record i AftaleEgenbetaltOmkost
						AftaleEgenbetaltOmkost ebomk = (AftaleEgenbetaltOmkost) PersistensService.opret(AftaleEgenbetaltOmkostImpl.class);
						ebomk.setAftale(aftale_);
						ebomk.setAfgaeldendefra(aftale.getGld());
						ebomk.setAfophoersdato(aftale.getOph());
						ebomk.setEgenbetaltomkostjn(egenBetaltOmkValgt);
						ebomk.setAftaleannulleretj_n(true);
						ebomk.setAnnulleretjn(true);
						ebomk.setAftaleophoertj_n(false);
						ebomk.setOphoertjn(false);
						ebomk.setUdfoertjn(false);

						PersistensService.save(ebomk);

						// Annullering af en aftale skal medføre her&nu tarifering... som sendes afsted i batch bufferen.
						aftale_.bestilStatusKoersel(false, false);

						// Kald program til automatisk udligning af afregninger ved annullering
						DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer().addJob("GUIBARANN", aftale_.getAftaleId(), null, null, aftale_, 8);

					}

					// Hvis vi er igang med ANNULLERING skal aftalen eller dens dækninger IKKE have ophørsdato -
					// det sørger natdrifen for.
					if (ophoerAnnullerAendre_ != GaiaConst.ANNULLERING) {
						BigDecimal tidlOrigOph = aftale_.getOph();
						aftale_.setOph(Datobehandling.datoPlusMinusAntal(getDato(), -1));

						behandlTidligereOphoer(aftale_, tidlOrigOph);

						// Filen må IKKE skrives hvis "Fuld ristorno" er = N da tilstedeværelsen af en record i denne fil er det samme som
						// at sige FULD RISTORNO = JA. Atributten "fuld ristorno" gemmes dog ikke databasen...
						if (fuldRistornoValgt == true) {
							AftaleEgenbetaltOmkost ebomk = PersistensService.opret(AftaleEgenbetaltOmkostImpl.class);
							ebomk.setAftale(aftale_);
							ebomk.setAfgaeldendefra(aftale_.getGld());
							ebomk.setAfophoersdato(aftale_.getOph());
							ebomk.setEgenbetaltomkostjn(egenBetaltOmkValgt);
							ebomk.setAftaleannulleretj_n(false);
							ebomk.setAnnulleretjn(false);
							ebomk.setAftaleophoertj_n(true);
							ebomk.setOphoertjn(true);
							ebomk.setUdfoertjn(false);

							PersistensService.save(ebomk);

							// Hvis aftalen sættes til ophør pr. seneste udført fornyelse / delopkrævning, skal der køres en tarifering samt automatisk udligning
							// hvis fuld ristorno er valgt
							aftale_.bestilStatusKoersel(false, false);
							DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer().addJob("GUIBARANN", aftale_.getAftaleId(), null, null, aftale_, 8);

						}
					}

					PersistensService.save(aftale_);
					saveAftalehaendelse();
					// OBS FEFA - gem af egenskab ved ophørs dialog (valg af sagsbehandler ved ophør dialog)
					if (aftaleEgenskabSystem != null) {
						aftaleEgenskabSystem.gem();
					}
				}
			}
			// Hvis getDato() == 0, og aftalen har en ophørsdato dvs. ophør skal fjernes...
			//else if(getDato().equals(new BigDecimal("0")) && getTilDato().intValue() != 0){
			if (ophoerAnnullerAendre_ == GaiaConst.FJERN_OPHOER) {
				Aftale aftale_ = (Aftale) getEntitet();
				if (aftale_ != null) {
					cahcedOldOphoerTilFjern = aftale_.getOph();
					aftale_.setOph(getDato());
					PersistensService.save(aftale_);
					saveAftalehaendelse();

					if (aftaleEgenskabSystem != null) {
						aftaleEgenskabSystem.gem();
					}
				}
			}
		}
		if (getParent() instanceof PanthaverBO) {
            ((PanthaverBO) getParent()).saveInGnAf();
        } else {
			DBServer.getInstanceSave().getVbsf().getRevideringsService().initDagbogAftalePersistensService(getAftale(), getDato(),
                    this.bonusrykningsDato_, this.dagsbogsflytningDisabled);
        }

		/**
		 * Kun oprettelse og ændring laver opdateringer i forbindelse med
		 * korttidsforsikringer.
		 * (OphoerAnnullerAendre bliver kun opdateret i setOphoer).
		 */
		if (isForlaengKorttid) {
			handleKorttid(true);

			StatusBO stsBO = null;
			Statustype[] statustyper = ((Aftaletype) getType()).getAftaletypensStatustyperNytegn(!isTilbud());
			for (int i = 0; statustyper != null && i < statustyper.length; i++) {
				if (!(statustyper[i].isStoptype())) {
					stsBO = new StatusBO(statustyper[i], this, forlaengKorttidFraDato);
					stsBO.setSelecteret(true);
					stsBO.save();
					break;
				}
			}
			Statustype type = StatustypeImpl.getOphoerstype();
			stsBO = new StatusBO(type, this, Datobehandling.datoPlusMinusAntal(forlaengKorttidTilDato, 1));
			stsBO.setSelecteret(true);
			stsBO.save();

			for (GenstandObjectIF gn : forlaengKorttidGenstandeTilOphoer) {
				PersistensService.save(gn);
			}

		} else if (ophoerAnnullerAendre_ == -1) {
			handleKorttid(isNyOphor);
		} else if (isKorttidsAftale() && ophoerAnnullerAendre_ == GaiaConst.AENDRE_OPHOER) {
			// Ændre ophør for korttidsforsikringer.
			// Tilføj ophørsstatustype... fordi den mangler! :-) Hilsen Long og Michael
			Statustype type = StatustypeImpl.getOphoerstype();
			StatusBO ophortStatusBO = new StatusBO(type, this, getDato());
			ophortStatusBO.setSelecteret(true);
			ophortStatusBO.save();
		}

		if (afregningFritekstModel != null) {
			afregningFritekstModel.save();
		}

		if (udlaegForlaengFleraarig) {
			udlaegForlaengHaendelse();
		}
	}

	private void behandlTidligereOphoer(Aftale aftale_, BigDecimal tidlOrigOph) {
		// OBSF5D2 Tidligere ophør er tilladt.
		if (aftale_.isSluttariferingIgangsat() &&
                tidlOrigOph.compareTo(aftale_.getOph()) > 0 &&
                isMulighedForTidligereOphoer()) {
            setStatusTyperTilTidligereOphoer();
            return;
        }

		if (isTidligereOphoerErTilladtVedAlleredeOphoertForsikring(aftale_, tidlOrigOph)){
            setStatusTyperTilTidligereOphoer();
            aftale_.bestilStatusKoersel(false, false);
        }
	}

	/**
	 * Forsikringer med afgangskode "Ophør Amorta" / "Ophør Sønderjysk" er sat til ophørt uden en tarifering.
	 * Dvs. der ikke findes nogen records i REPDOPH.
	 * Dette tjek for at der stadig er mulighed for at ophørsdato kan ændres på disse forsikringer.
	 */
	private boolean isTidligereOphoerErTilladtVedAlleredeOphoertForsikring(Aftale aftale_, BigDecimal tidlOrigOph) {
		return isNaersikringAmorta() && isRigtigOphoert(aftale_) && isAfgangsoplysningOphoertAmortaSoenderjysk(aftale_) && isTidligereOphoertAftale(aftale_, tidlOrigOph) ;
	}

	private boolean isNaersikringAmorta(){
		return "NE AM".contains(DBServer.getInstance().getDatabasePrefixExclMiljoe());
	}
	boolean isAfgangsoplysningOphoertAmortaSoenderjysk(Aftale pAftale){
		Opsigelse opsigelse = pAftale.getOpsigelseNyeste();
		if (opsigelse == null) return false;
		String ostpKortBenaevnelse = opsigelse.getOpsigelsestype().getKortBenaevnelse().trim();
		return ostpKortBenaevnelse.equals("OPHØRAMOR") || ostpKortBenaevnelse.equals("OPHØRTSØN");
	}
	private boolean isTidligereOphoertAftale (Aftale pAftale, BigDecimal tidlOrigOph){
		return tidlOrigOph.compareTo(pAftale.getOph()) > 0;
	}
	private boolean isRigtigOphoert(Aftale pAftale){
		return pAftale.getAftaleStatustypeNyesteUdenOphoer().getStatustype().isOphoertStatus();
	}

	/**
	 * Klienten har bestemt, at fremtidige adresser skal med selvom de ikke findes som bo'er.<br>
	 *     Det er ikke optimalt, men vi SKAL have adresseskift med over
	 */
	private void saveFremtidigeAdresser() {
		AftaleImpl aftale = DBServer.getInstance().getVbsf().lookup(AftaleImpl.class, getEntitetIdBefore());
		AftaleOmraade[] afors = aftale != null ? aftale.getAftaleOmraade() : null;

		List<AdresseBO> adresser = getAdresser();

		// TODO vurder om det ikke skal betinges af gældende afad, eller i hvert fald at typen tillader forsikringssted.

		for (AftaleAdresse afad : fremtidigeLovligeAdresser){
			boolean isAktuel = contains(adresser, afad.getAdresse());
			if (isAktuel)
				continue;
				// værn mod dobbeltoprettelse af adresse

			afad.setAftale((Aftale) getEntitet());
			PersistensService.gem(afad);

			if (afors != null){
				// + (tarif)område pr. samme dato
				for (AftaleOmraade afor : afors ){
					if (afad.getGld().compareTo(afor.getGld()) == 0 && !afor.isAnnulleret()){
						afor.setAftale(afad.getAftale());
						PersistensService.gem(afor);
					}
				}
			}
		}
	}

	private boolean contains(List<AdresseBO> adresser, Adresse adresse) {
		for (AdresseBO adbo : adresser){
			if (adbo.isSelecteret() && adbo.getAdresse() != null){
				if (adbo.getAdresse().equals(adresse))
					return true;
			}
		}
		return false;
	}

	/**
	 * For PanthaverdeklarationsBO afgøres om der er sket ændring i deklarationsprioriteten og hvis ja <br>
	 * forsøges afsendt en Z60 Prioritetsændring
	 */
	private void handleOmprioriteringPanthaverdeklaration(AftaleEgenskabSystem pEgenskabsystem) {
		if (getParent() instanceof PanthaverBO && pEgenskabsystem != null) {
			String oldvaerdi = pEgenskabsystem.getPanthaverdeklarationsprioritetPersisteret();
			if (oldvaerdi != null) {
				PanthaverBO panthaverBO = (PanthaverBO) getParent();
				if (panthaverBO.isEdiReady()) {
					EgenskabAttribut egattr = pEgenskabsystem.getEgenskabAttribut(Aftaleegngrp.PH_DEKLARATIONS_PRIO);
					String nyvaerdi = egattr.getVaerdiPresentation();
					if (!oldvaerdi.equals(nyvaerdi)) {
						int nyPrioritet = -1;
						try {
							nyPrioritet = Integer.parseInt(nyvaerdi);
						} catch (NumberFormatException e) {
						}
						if (nyPrioritet >= 1 && nyPrioritet <= 4) {
							// send Z60 hvis muligt
							EdiPanthaverService.getInstance().udfoerPrioritetsaendring(this, nyPrioritet);
						}
					}
				}
			}
		}
	}

	/**
	 * For PanthaverdeklarationsBO afgøres om der er sket ændring i deklarationsnummer og hvis ja <br>
	 * håndteres evt. dækningsgenoptagelse<p>
	 */
	private void handleDeklarationsnummerAendringPanthaverdeklaration(AftaleEgenskabSystem pEgenskabsystem) {
		/**
		 * PK 12. juni 2013 Jeg lavede dette i 2009, men i dag forstår jeg ikke helt relevansen af metoden.
		 * Hvornår ændrer man deklarationsnummer på en gsprodeklaration? Er det et redskab til at få fejl-etablerede
		 * deklarationer på plads?
		 */
		if (getParent() instanceof PanthaverBO && pEgenskabsystem != null) {
			AftaleBO aftaleBO = getParent().getAftaleBO();
			if (!aftaleBO.ediKommunikationEnabled_)
				return;
			String oldvaerdi = null;
			EgenskabAttribut egat = pEgenskabsystem.getEgenskabAttribut(Aftaleegngrp.PH_DEKLARATIONS_NR);
			if (egat != null) {
				Egenskab oldEgenskab = egat.getValgtEgenskab();
				if (oldEgenskab != null) {
					oldvaerdi = oldEgenskab.formatToDisplay();
				} else if (egat.getVaerdiPresentation() != null) {
					oldvaerdi = egat.getVaerdiPresentation();
					// HACK bør ikke være nødvendigt, men det er det U-1E686
				}
			}
			PanthaverBO panthaverBO = (PanthaverBO) getParent();
			if (panthaverBO.isEdiReady()) {
				String nyvaerdi = egat.getVaerdiPresentation();
				if ((oldvaerdi == null && nyvaerdi != null) ||
						(oldvaerdi != null && !oldvaerdi.equals(nyvaerdi))) {
					GenstandBO gnbo = (GenstandBO) panthaverBO.getParent();
					if (gnbo != null) {
						List<DaekningBO> dkbos = gnbo.getDaekninger();
						if (dkbos != null) {
							for (DaekningBO dkbo : dkbos) {
								if (dkbo.isSelecteret() && dkbo.isPanthaverRelevant()) {
									((AftaleBO) gnbo.getParent()).addDaekningTilNyeListe(dkbo);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean skipSaveAftalehaendelse() {
		if (isPanthaveraftale())
			return true;  // Aldrig aftalehændelse på panthaverdeklarationer
		return skipSaveAftalehaendelse_;
	}

	/**
	 * @return true hvis parent er et PanthaverBO.
	 */
	public boolean isPanthaveraftale() {
		return this.getParent() instanceof PanthaverBO;
	}

	/**
	 * Oprette evt. speciel oprettelsesgebyr ved ny oprettelse af en forsikring eller ved vognskift.<br>
	 * <p>
	 * Gebyr oprettes hvis der findes en aktiv regel med hændelsestype 'OPRETTELSE_GEBYR'<br>
	 * Aktiv regel med hændelsestype 'OPRETTELSE_GEBYR' bruges generelt. <br>
	 * Men hvis der også findes en aktiv regel med hændelsestype 'OPRETTELSE_GEBYR_KNA' så skal den bruges til Knallert<br>
	 * Selskab kan selv oprette ovenstående regler under Delsystemer/Regler/Gebyrtyper... <br>
	 * <p>
	 * Udover ovenstående regler skal der også findes relevant regel i Aftaletype <-> Omkosningstype med omkostningstypen's kortbenævnelse = "OPRETGEBYR".
	 *
	 * Ass. 39521 - Hvis Pensam så oprettes gebyr KUN når kunden ikke tilmeldt e-Boks.
	 */
	private void saveSpecielOprettelsesGebyr() {
		Aftale af = getAftale();
		if (!af.harOprettelsesGebyrOmkostningstypeRegler(getDato())) {
			return;
		}

		if (AlleTillaegsfunktionaliteter.GEBYR_VED_NYTEGNING_OG_FORNYELSE_GAELDER_KUN_FOR_KUNDER_UDEN_EBOKS.isPresent()){
			if (aftale.getTegnesAfIndivid().isTilmeldtEBoks()) return;
		}

		GebyrtypeRegel gebyrtypeGenerel = GebyrtypeRegelImpl.getGebyrtype(GebyrtypeRegel.HAENDELSE_OPRETTELSES_GEBYR);
		GebyrtypeRegel gebyrtypeKNA = GebyrtypeRegelImpl.getGebyrtype(GebyrtypeRegel.HAENDELSE_OPRETTELSES_GEBYR_KNA);
		if (gebyrtypeGenerel == null && gebyrtypeKNA == null) {
			return;
		}
		if (gebyrtypeGenerel != null && gebyrtypeKNA == null) {
			gebyrtypeKNA = gebyrtypeGenerel;
		}

		//Opret Gebyr
		Gebyr gebyr = PersistensService.opret(GebyrImpl.class);
		if (af.getAftaletypeKortBenaevnelse().trim().equals(Aftaletype.KNALLERT_FORSIKRING)) {
			gebyr.setBenaevnelse(gebyrtypeKNA.getBenaevnelse());
			gebyr.setBeloeb(gebyrtypeKNA.getBeloeb());
			gebyr.setGebyrtypeRegel(gebyrtypeKNA);
		} else {
			gebyr.setBenaevnelse(gebyrtypeGenerel.getBenaevnelse());
			gebyr.setBeloeb(gebyrtypeGenerel.getBeloeb());
			gebyr.setGebyrtypeRegel(gebyrtypeGenerel);
		}
		gebyr.setIndivid(af.getTegnesAfIndivid());
		gebyr.setAftale(af);
		PersistensService.gem(gebyr);
		//Selve Gebyrafregning bliver oprettet først ved tarifering af forsikringen
	}

	public Aftalehaendelse saveAftalehaendelse() {
		return saveAftalehaendelsenPrDato(getDato());
	}

	public Aftalehaendelse saveAftalehaendelse(BigDecimal pDato) {
		return saveAftalehaendelsenPrDato(pDato);

	}

	public Aftalehaendelse saveAftalehaendelsenPrDato(BigDecimal pDato) {
		if (skipSaveAftalehaendelse()) {
			log_.info(GensamUtil.getMemLabel() + "1278 Der saves ingen aftalehændelse jf. setting ");
			return null;
		}
		// Er flyttet til efter skipSaveAftalehændelse.
		// Der blev udlagt signal til informationsudtrækket på datoer hvor der ingen ændringer eller beregninger var, så nu er vi da sikker på at der i det mindste er en hændelse.
		if (!isPanthaveraftale()) {
			UdtraekInformationssystemHelper.tjekOpretUdtraekInformationssystemAftale((Aftale) getEntitet(), pDato);
		}

		Aftalehaendelse ah = PersistensService.opret(AftalehaendelseImpl.class);
		ah.setAftale((Aftale) getEntitet());

		BigDecimal haendelsesDato = pDato;

		// Specielt ved fjern ophør - her skal der "gamle" ophørsdato gemmes som hændelsesdato
		if (ophoerAnnullerAendre_ == GaiaConst.FJERN_OPHOER) {
			haendelsesDato = cahcedOldOphoerTilFjern;
		} else {
			if (pDato.compareTo(getFraDato()) < 0 &&
					getFraDato().compareTo(getEntitet().getGld()) == 0) {
				haendelsesDato = getFraDato();
				// #12906 Ved genoptag forsikring bliver hændelsen en dag for gammel
				// Bo'et er loaded pr. dagen før ikraft og getFraDato indeholder den nye ikraft.
				// Kan ikke gennemskue om dette kan udføres ubetinget eller kun betinget af sidste halvdel af if
			}
		}

		ah.setHaendelsesdato(haendelsesDato);
		ah.setBesoegsdato(getSatsDato());

		ah.setHaendelsestype(getAftalehaendelsestypeTilSave());
		ah.setPoliceUdskriftFravalgt(isPoliceUdskriftFravalgt(pDato));
		PersistensService.save(ah);
		getAftale().addAftalehaendelse(ah);
		aftalehaendelseSaved_ = ah;

		// Tilknyt og gem hændelsesbemærkning til ære for overbliksbilledet.
		if (aftalehaendelseBemaerkning != null && aftalehaendelseBemaerkning.equals("") == false) {
			AftalehaendelseBemaerkning bem = PersistensService.opret(AftalehaendelseBemaerkningImpl.class);
			bem.setTekst(aftalehaendelseBemaerkning);
			bem.setAftalehaendelse(ah);

			PersistensService.save(bem);
		}
		return ah;
	}


	public void setAftalehaendelseBemaerkning(String pBemaerkning) {
		aftalehaendelseBemaerkning = pBemaerkning;
	}

	/**
	 * Returnere en liste af ForsikringstagerAftalehaendelseProvModtBO som har gældende pr. medsendt dato.
	 *
	 * @param pDato
	 * @return
	 */
	public List<ForsikringstagerAftalehaendelseProvModtBO> getProvisionsModtagereMedGldPr(BigDecimal pDato) {
		List<ForsikringstagerAftalehaendelseProvModtBO> retur = new ArrayList<ForsikringstagerAftalehaendelseProvModtBO>();
		int antalProvModtagereFundet = 0;
		if (getEntitet() != null) {
			Aftalehaendelse nyeste = ((Aftale) getEntitet()).getAftalehaendelseNyeste(pDato, true);
			if (nyeste != null) {
				ForsikringstagerAftalehaendelseProvisionmodtager[] provModtagere = nyeste.getForsikringstagerAftalehaendelseProvisionmodtager();
				if (provModtagere != null && provModtagere.length > 0) {
					for (int i = 0; i < provModtagere.length; i++) {
						ForsikringstagerAftalehaendelseProvisionmodtager f = provModtagere[i];
						retur.add(new ForsikringstagerAftalehaendelseProvModtBO(f, this, pDato));
						antalProvModtagereFundet++;
					}
				}
				// Hvis der ikke findes persistent data til antallet af definerede provisionsmodtagere, opret "tomme" BO'er til resten
				if (antalProvModtagereFundet < GaiaConst.ANTAL_PROVISIONSMODTAGERE) {
					for (int m = 0; m < GaiaConst.ANTAL_PROVISIONSMODTAGERE; m++) {
						boolean prvModtFundet = false;
						for (int i = 0; i < retur.size(); i++) {
							if (retur.get(i).getSekvens().intValue() == m + 1) {
								prvModtFundet = true;
							}
						}
						if (!prvModtFundet) {
							retur.add(new ForsikringstagerAftalehaendelseProvModtBO(this, pDato, new BigDecimal(m + 1)));
						}
					}
				}
			} else {
				// Der findes ingen tilknyttede provisionsmodtagere - opret "tomme" BO'er
				for (int p = 0; p < GaiaConst.ANTAL_PROVISIONSMODTAGERE; p++) {
					retur.add(new ForsikringstagerAftalehaendelseProvModtBO(this, Datobehandling.getDagsdatoBigD(), new BigDecimal(p + 1)));
				}
			}
		}
		return retur;
	}

	/**
	 * Finder nyeste sæt med gld. pr. BO'ets getDato()
	 *
	 * @return
	 */
	public ProvisionsModtagerSaetHolder getNyesteIndividProvisionsModtagerSaetHolder() {
		if (provModtagerSaetHolder == null) {
			List<ForsikringstagerAftalehaendelseProvModtBO> provModtagere = getProvisionsModtagereMedGldPr(getDato());
			provModtagerSaetHolder = new ProvisionsModtagerSaetHolder(provModtagere, getDato(), this);
			if (DBServer.getInstance().getSelskabsOplysning().medProvisionsmodtagerAftaleNiveau()) {
				if (provModtagerSaetHolder.isEmpty() &&
						getAftale() != null && getAftale().findesProvisionsmodtagerPaaForsikringsNiveau()) {
					provModtagere = getProvisionsModtagerPaaForsikring();
					provModtagerSaetHolder = new ProvisionsModtagerSaetHolder(provModtagere, getDato(), this);
				}
			}
			if (provModtagerSaetHolder.isEmpty()) {

				// Vi er igang med en aftaleændring.. undersøg om det er det forrige sæt oplysninger der skal vises
				if (!isNew()) {
					provModtagerSaetHolder = this.getIndivid().getGldIndividProvisionsModtagerSaetHolderTilFunktioner(this);
				} else {
					// 	Hvis der ikke findes nogle på aftalehændelse, brug dem tilknyttet på individniveau
					provModtagerSaetHolder = this.getIndivid().getNyesteIndividProvisionsModtagerSaetHolder();
				}
			}
		}
		List<ForsikringstagerAftalehaendelseProvModtBO> provList = provModtagerSaetHolder.getBoListe();
		for (int i = 0; i < provList.size(); i++) {
			ForsikringstagerAftalehaendelseProvModtBO bo = provList.get(i);
			bo.setIsNew(true);
			bo.setEntitet(null);
			bo.setParent(this);
		}
		return provModtagerSaetHolder;
	}

	public List<ForsikringstagerAftalehaendelseProvModtBO> getProvisionsModtagerPaaForsikring() {
		List<ForsikringstagerAftalehaendelseProvModtBO> provModtagere = new ArrayList<ForsikringstagerAftalehaendelseProvModtBO>();
		for (int i = 1; getAftale() != null && i <= GaiaConst.ANTAL_PROVISIONSMODTAGERE; i++) {
			ForsikringstagerAftalehaendelseProvisionmodtager fap = getAftale().getProvisionsmodtagerPaaForsikring(new BigDecimal(i));
			ForsikringstagerAftalehaendelseProvModtBO fapbo = null;
			if (fap != null) {
				fapbo = new ForsikringstagerAftalehaendelseProvModtBO(fap, this, getDato());
			} else {
				fapbo = new ForsikringstagerAftalehaendelseProvModtBO(this, getDato(), new BigDecimal(i));
			}
			provModtagere.add(fapbo);
		}
		return provModtagere;
	}

	public void setProvSaetHack(ProvisionsModtagerSaetHolder pSaet) {
		provModtagerSaetHolder = pSaet;
	}

	/**
	 * Finder nyeste sæt med gld. pr medsendte dato
	 *
	 * @return
	 */
	public ProvisionsModtagerSaetHolder getNyesteIndividProvisionsModtagerSaetHolder(BigDecimal pDato) {
		List provModtagere = getProvisionsModtagereMedGldPr(pDato);
		ProvisionsModtagerSaetHolder provModtagerSaetHolder = new ProvisionsModtagerSaetHolder(provModtagere, pDato, this);
		return provModtagerSaetHolder;
	}

	/**
	 * Funktionen er en forlængelse af {@link #save}, der tager sig af de
	 * forhold der gør sig gældende i forbindelse med korttidsaftaler.
	 * <p>
	 * Situationer:
	 * <UL>
	 * <LI> Skift fra korttidsforsikring til almindelig uden ophør, dog inden
	 * tarifering (fjern ophør).
	 * <LI> Skift fra almindelig forsikring til korttidsforsikring (set ophør),
	 * heruder fjernelse af tilknyttede forfald og frekvens objekter.
	 * <LI> Ny ophørsdato for korttidsaftaler (skift ophør).
	 * </UL>
	 * <p>
	 *
	 * @see #setOphoer(BigDecimal, int)
	 * @see #isKorttidsAftale()
	 * @see GaiaConst#OPHOER
	 * @see GaiaConst#AENDRE_OPHOER
	 * @see GaiaConst#FJERN_OPHOER
	 */
	protected void handleKorttid(boolean isNyOphor) {
		if (adfaerdStrategy_ != null && adfaerdStrategy_ instanceof AdfaerdStrategyGsproGsproHistorik) {
//    		log_.error("HandleKorttid disabled for historikkonverteringer ");
			return;
		}

		/**
		 * Nytegning. Korttidsaftaler sættes til ophør med det samme og får
		 * tilknyttet ophørt status.
		 */
		if (isNew() && isSelecteret() && isKorttidsAftale()) {
			/** @todo Denne del af if'en kan undværes når todo'en under
			 *  tempTilDato kan fjernes idet aftale-bo-save strukturen løser
			 *  problemet med at sætte korrektet ophørsdatoer.
			 */
			setOphoer(getTilDato(), GaiaConst.OPHOER);

			// Findes der en ophørt-statustype gøres kort process... på med den!
			// Men ikke hvis det er et tilbud
			if (!(isNytTilbud() || this.getAftale().harKunTilbudsstatustyper())) {
				StatusBO ophortStatusBO = getStatusOphor();
				if (ophortStatusBO != null) {
					ophortStatusBO.setSelecteret(true);
					ophortStatusBO.save();
				}
			}

		} else if (!isNew() && isNyOphor) {
			/**
			 * Ophør/årsbaseret præmie er ændret på persistent forsikring.
			 *
			 * Hvis årsbaseret præmie er ændret på persistent forsikring vil
			 * det altid give en ny ophørsdato.
			 */

			if (getTilDato().intValue() > 0) {
				// Til dato skal korrigeres med en dag i kald til setOphoer()
				setOphoer(Datobehandling.datoPlusMinusAntal(getTilDato(), 1), GaiaConst.OPHOER);
			} else {
				setOphoer(getTilDato(), GaiaConst.FJERN_OPHOER);
			}
		} else if (!isNew() && !isNyOphor && isKorttidsAftale() && !this.getAftale().isTilbud()
				&& !this.getAftale().findesStatustypeAfventGodkendt() && !this.getAftale().findesStatustypeGodkendt()) {
			Statustype[] statustyper = ((Aftaletype) getType()).getAftaletypensStatustyperNytegn(true);
			Statustype stsTpNytgn = null;
			for (int i = 0; statustyper != null && i < statustyper.length; i++) {
				if (!(statustyper[i].isStoptype())) {
					stsTpNytgn = statustyper[i];
					break;
				}
			}

			boolean opretNySts = false;
			StatusBO[] statusBOer = getStatus();
			if (statusBOer != null) {
				for (StatusBO stsBO : statusBOer) {
					if (stsBO.getEntitet() != null && stsBO.getEntitet().getGld().compareTo(getDato()) == 0) {
						opretNySts = true; // Ændring pr. tilladt ændringsdato
						if (!stsBO.getEntitet().isOphUdfyldt()) {
							stsBO.setSelecteret(false);
						}
					}
				}
			}
			if (opretNySts) {
				StatusBO stsBO = new StatusBO(stsTpNytgn, this, getDato());
				stsBO.setSelecteret(true);
				stsBO.save();
			}
		}

		/**
		 * Fjern forfald og frekvens for korttidsaftaler.
		 */
		if (isKorttidsAftale()) {

			// gem tildato for children objekter.
			tempTilDato = getTilDato();
			setTilDato(new BigDecimal(0));

			// Korttidsaftaler skal ikke have hovedeforfald+frekvens.
			BusinessObject bo = BusinessObject.getSelected(getFrekvens());
			if (bo != null) bo.setSelecteret(false);

			bo = BusinessObject.getSelected(getForfald());
			if (bo != null) bo.setSelecteret(false);
		}
	}

	/**
	 * Hvis load har identificeret betingede ydelser bliver disse YdelseBO'er savet her
	 * efter at alt andet er savet
	 */
	public void saveYdelserBetingede(List<YdelseBO> ydelserBOBetingede) {
		for (int i = 0; ydelserBOBetingede != null && i < ydelserBOBetingede.size(); i++) {
			(ydelserBOBetingede.get(i)).saveYdelserBetingede();
		}
	}

	public void addToCollection(Aftale pAftale) {
		((IndividImpl) getIndivid().getEntitet()).addAftale((AftaleImpl) pAftale);
	}

//	public void addToCollection(AftalesAftp pAftalesAftp) {
//		((Aftale)getEntitet()).addAftalesAftp(pAftalesAftp);
//	}

	public void addToCollection(Hovedprodukt pHovedprodukt) {
		((AftaleImpl) getEntitet()).addHovedprodukt((HovedproduktImpl) pHovedprodukt);
	}

	/**
	 * Nogle selskaber har ikke tilknyttet status på deres aftaler.
	 *
	 * @deprecated hvorfor spørger du om det - er altid sand undtagen panthaverdeklarationer.
	 */
	public boolean hasStatus() {
		loadStatus();
		return hasStatus;
	}

	public StatusBO[] getStatus() {
		loadStatus();
		return status;
	}

	/**
	 * I forbindelse med korttidsaftaler er det nødvendigt at tilknytte en
	 * tredie statustype - Klarophørt og den findes via denne funktion.
	 * <p>
	 *
	 * @return StatusBO, med korrekte type og startdato sat til aftalen tildato.
	 * <code>null</code> hvis ikke der kan findes en passende type.
	 * @see Aftale#getAftaleTypen()
	 * @see StatustypeImpl#getOphoerstype()
	 */
	protected StatusBO getStatusOphor() {
		//Statustype type = ((Aftale)getEntitet()).getAftaleTypen().getStatustypeAfType(StatustypeImpl.getOphoerstype());
		Statustype type = StatustypeImpl.getOphoerstype();

		if (type != null) {

			return new StatusBO(type, this,
					Datobehandling.datoPlusMinusAntal(getTilDato(), 1));
		}

		return null;
	}

	/**
	 * Initialisering af status objekterne- der er altid kun to en <code>
	 * stoptype</code> og en <code>ikke-stoptype</code> (godkendt/afventer
	 * godkendelse).
	 * <p>
	 * Er aftalen nyoprettet loades de 2 oprettelsestyper, ellers loades
	 * ikke-oprettelsestyper.
	 * <p>
	 * Ved nyoprettelses sættes det StatusBO med Statustypen der ikke er en
	 * stoptype til <code>selecteret</code>. Dvs. at ved nyoprettelse får
	 * aftaler automatisk tilknyttet statustypen <i>Godkendt</i>.
	 * <p>
	 * Er aftalen med persistensstatus ny undersøges om aftalens statustype
	 * er en oprettelsestype. (Aftalen kan være gemt på databasen, men endnu
	 * ikke behandlet af natdriften).
	 * <p>
	 * Har aftalen ikke tilknyttet en statustype eller findes der i regelsættet
	 * ingen lovlige typer sættes hasStatus = false.
	 */
	protected void loadStatus() {

		// Første gang er status null og hasStatus true, anden gang vil
		// enten status være et array ellers vil hasStatus være false.
		boolean isLoadet = (status != null || !hasStatus || (getParent() instanceof PanthaverBO));

		if (this.adfaerdStrategy_ != null && !this.adfaerdStrategy_.loadStatusBO()) {
			isLoadet = true;
			// håndteres af klienten
		}

		if (isLoadet || !statusLoadEnabled) {
			return;
		}

		// find typen én gang for alle.
		AftaleStatustype ast = null;
		if (aftale != null) {
			ast = aftale.getAftaleStatustypenMedGld(getDato());
			if (ast == null) {
				ast = aftale.getAftaleStatustypeNyesteUdenOphoer();
			}

		}

		// Hvis aftalen er ny og der findes statustyper tilknyttet aftaletypen
		// skal der hentes oprettelsestyper.
		if (isNew()) {
			if (nyStatusTypeCaller == GaiaConst.STATUS_TYPE_OPRETTELSE_AF_ACCEPTER) {
				AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
				aftaleStatustype.setAftale(this.getAftale());
				aftaleStatustype.setStatustype(StatustypeImpl.getStatusType(Statustype.STATUS_TYPE_TILBUD_ACC));
				aftaleStatustype.setGld(getDato());
				aftaleStatustype.setStatustekst(nyStatusTypeCallerTekst);
				PersistensService.gem(aftaleStatustype);
				this.getAftale().addAftaleStatustype(aftaleStatustype);

				StatusBO bo = new StatusBO(aftaleStatustype, this, getDato());
				bo.setSelecteret(true);

				status = new StatusBO[]{bo};
				nyStatusTypeCaller = -1;
			} else if (nyStatusTypeCaller == GaiaConst.STATUS_TYPE_OPRETTELSE_AF_GENOPLIV) {
				AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
				aftaleStatustype.setAftale(this.getAftale());
				aftaleStatustype.setStatustype(StatustypeImpl.getStatusType(Statustype.STATUS_TYPE_TILBUD));
				aftaleStatustype.setGld(getDato());
				aftaleStatustype.setStatustekst(nyStatusTypeCallerTekst);
				PersistensService.gem(aftaleStatustype);
				this.getAftale().addAftaleStatustype(aftaleStatustype);

				StatusBO bo = new StatusBO(aftaleStatustype, this, getDato());
				bo.setSelecteret(true);

				status = new StatusBO[]{bo};
				nyStatusTypeCaller = -1;
			} else if (nyStatusTypeCaller == GaiaConst.STATUS_TYPE_OPRETTELSE_AF_AFVENTGODKEND) {
				AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
				aftaleStatustype.setAftale(this.getAftale());
				aftaleStatustype.setStatustype(StatustypeImpl.getStatusType(Statustype.STATUS_TYPE_AFV_GODKENDT));
				aftaleStatustype.setGld(getDato());
				aftaleStatustype.setStatustekst(nyStatusTypeCallerTekst);
				PersistensService.gem(aftaleStatustype);
				this.getAftale().addAftaleStatustype(aftaleStatustype);

				StatusBO bo = new StatusBO(aftaleStatustype, this, getDato());
				bo.setSelecteret(true);

				status = new StatusBO[]{bo};
				nyStatusTypeCaller = -1;
			} else if (nyStatusTypeCaller == GaiaConst.STATUS_TYPE_OPRETTELSE_AF_GODKEND) {
				AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
				aftaleStatustype.setAftale(this.getAftale());
				aftaleStatustype.setStatustype(StatustypeImpl.getStatusType(Statustype.STATUS_TYPE_GODKENDT));
				aftaleStatustype.setGld(getDato());
				aftaleStatustype.setStatustekst(nyStatusTypeCallerTekst);
				PersistensService.gem(aftaleStatustype);
				this.getAftale().addAftaleStatustype(aftaleStatustype);

				StatusBO bo = new StatusBO(aftaleStatustype, this, getDato());
				bo.setSelecteret(true);

				status = new StatusBO[]{bo};
				nyStatusTypeCaller = -1;
			} else {
				Statustype[] statustyper = ((Aftaletype) getType()).getAftaletypensStatustyperNytegn(!isNytTilbud);
				if (statustypeToUseUansetHvad != null) {
					int ant = statustypeToUseUansetHvad.isOphoertGruppen() ? 2 : 1;
					statustyper = new Statustype[ant];
					statustyper[ant - 1] = statustypeToUseUansetHvad;
					if (ant > 1) {
						statustyper[0] = StatustypeImpl.getStatusType(Statustype.IKRAFT);
					}
				}

				if (statustyper != null && statustyper.length > 0) {
					ArrayList<StatusBO> l = new ArrayList<>(statustyper.length);

					// Find og gem alle oprettelsestyper
					for (int i = 0; i < statustyper.length; i++) {

						BigDecimal statusDato = getDato();
						if (statustyper[i].isOphoertGruppen())
							statusDato = Datobehandling.datoPlusMinusAntal(getTilDato(), 1);
						StatusBO bo = new StatusBO(statustyper[i], this, statusDato);
						l.add(bo);

						// Hvis aftalen er ny så kan den måske være nyt tilbud ...
						if (!statustyper[i].isStoptype() && !this.isNytTilbud && !statustyper[i].isTilbud()) {
							bo.setSelecteret(true);
						}

						// ... så brug status TILBUD hvis nyt tilbud
						if (this.isNytTilbud && statustyper[i].isTilbud()) {
							bo.setSelecteret(true);
						}
						if (statustyper.length == 1 && statustypeToUseUansetHvad != null) {
							bo.setSelecteret(true);
						}
						if (statustyper.length == 2 && statustypeToUseUansetHvad != null) {
							if (statustyper[1].getKortBenaevnelse().trim().equals(Statustype.ANNULLERET))
								l.get(0).setTilDato(statusDato);
						}
					}

					status = ContainerUtil.toArray(l);
				}
			}
		} else {

			ArrayList<StatusBO> l = new ArrayList<StatusBO>();
			if (ast != null) {
				// Kun i det tilfælde hvor ophør ændres tilbage i tid
				boolean aendreKorttidTilbage = ophoerAnnullerAendre_ == GaiaConst.AENDRE_OPHOER
						&& getAftale().getOph().compareTo(getDato()) == 1;

				// I disse tilfælde fremsøges den aktuelle statustype lidt længere nede
				if (ophoerAnnullerAendre_ != GaiaConst.OPHOER
						&& ophoerAnnullerAendre_ != GaiaConst.ANNULLERING
						&& !aendreKorttidTilbage) {

					l.add(new StatusBO(ast, this, getDato()));
				}

				Statustype type = null;

				if (ophoerAnnullerAendre_ == GaiaConst.OPHOER) {
					AftaleStatustype[] typer = ((Aftale) getEntitet()).getAftaleStatusTyperTilOphoer(getDato());
					if (typer != null) {
						for (int t = 0; t < typer.length; t++) {
							l.add(new StatusBO(typer[t], this, getDato()));
						}
					}
					type = StatustypeImpl.getOphoerstype();
				} else if (ophoerAnnullerAendre_ == GaiaConst.ANNULLERING) {
					AftaleStatustype[] typer = ((Aftale) getEntitet()).getAftaleStatusTyperTilAnnullering();
					if (typer != null) {
						for (int t = 0; t < typer.length; t++) {
							l.add(new StatusBO(typer[t], this, getDato()));
						}
					}
					type = StatustypeImpl.getAnnulleringstype();
				} else if (ophoerAnnullerAendre_ == GaiaConst.AENDRE_OPHOER) {
					// Her anvendes den 'nye' oph.dato til fremsøgning af fremtidige statustyper.
					// Ved ændring frem i tid finder denne metode IKKE
					// statustypen der var tilknyttet på ændringstidspunktet
					// (ast) fordi tempNyOphDato (= getDato = ændringsdato) er
					// mindre end den aktuelle statustypes gældende
					AftaleStatustype[] typer = ((Aftale) getEntitet()).getAftaleStatusTyperTilOphoer(tempNyOphDato);
					for (int t = 0; typer != null && t < typer.length; t++) {
						l.add(new StatusBO(typer[t], this, tempNyOphDato));
					}
				}


				if (type != null) {
					StatusBO y = new StatusBO(type, this, getDato());
					l.add(y);
				}
				status = ContainerUtil.toArray(l);
			}
		}

		hasStatus = status != null && status.length > 0;
	}

	/**
	 * Funktionen virker KUN i forbindelse med nyoprettelse og ændring af forsikringer og ikke tilbud.
	 * Nov. 2016: jo virker nu for tilbud.
	 *
	 * Skifter status på baggrund af om den aktuelle statustype skal være en
	 * stoptype. Der må kun være to mulige stoptyper at vælge i mellem.
	 *
	 * @return det selekterede StatusBO, null hvis ingen
	 */
	public StatusBO setStatusStoptype(boolean isStoptype) {
		StatusBO[] statusboer = getStatus();
		StatusBO svar = null;
		for (int i = 0; i < statusboer.length; i++) {
			StatusBO statusBO = statusboer[i];
			Statustype statustype = (Statustype) statusBO.getType();
			if (statustype.isEnAfTilbudTyper()) {
				statusBO.setSelecteret(false);
				if (isTilbud() || statusboer.length == 1) {
					statusBO.setSelecteret(true);
					svar = statusBO;
				}
				continue;
			}
			boolean stoptype = statusBO.isStoptype();
			statusBO.setSelecteret(stoptype == isStoptype);
			if (statusBO.isSelecteret())
				svar = statusBO;
		}
		return svar;
	}

	/**
	 * Ændre status til afventer ændring pr. ændringsdatoen samt hvis der findes en godkendt ændring så sættes den til ophør.
	 */
	public void setStatusAfventerAendring() {
		boolean statusFundet = false;
		StatusBO[] statuser = getStatus();
		for (int i = 0; statuser != null && i < statuser.length; i++) {
			if (statuser[i].getType().getKortBenaevnelse().trim().equals(Statustype.AFVAENDR)) {
				statuser[i].setSelecteret(true);
				statusFundet = true;
				if (!statuser[i].isNew() && statuser[i].getEntitet().getGld().compareTo(getDato()) != 0) {
					statuser[i].setFraDato(getDato());
					statuser[i].clearPersistens();
				}
			} else {
				if (statuser[i].isSelecteret() && statuser[i].getType().getKortBenaevnelse().trim().equals(Statustype.KLARAENDR)) {
					statuser[i].setSelecteret(false);
				}
			}
		}
		if (!statusFundet) {
			List<StatusBO> statusListe = new ArrayList<StatusBO>(ContainerUtil.asList(statuser));
			StatusBO nyStatus = new StatusBO(StatustypeImpl.getStatusType(Statustype.AFVAENDR), this, getDato());
			nyStatus.setSelecteret(true);
			statusListe.add(nyStatus);
			status = ContainerUtil.toArray(statusListe);
			hasStatus = true;
		}
	}

	/**
	 * Henter alle Aftale-statustype relationer.
	 */
	public List<StatusBO> getAftaleStatustyper() {
		if (aftaleStatustyper != null) {
			return aftaleStatustyper;
		}

		aftaleStatustyper = new ArrayList<StatusBO>();

//		List result = new ArrayList();
		AftaleStatustype astArray[] = aftale.getAftaleStatustype(null);

		for (int i = 0; astArray != null && i < astArray.length; i++) {
			aftaleStatustyper.add(new StatusBO(astArray[i], this, getDato()));
		}

		return aftaleStatustyper;
	}

	/**
	 * Hent individtype/aftaletype forholdsbeskrivelser med aftalens type som
	 * argument.
	 */
	protected IntpAftp[] getIndividtypeAftaletyper() {
		if (individtypeaftaletyper == null) {
			individtypeaftaletyper = ((Aftaletype) getType()).getIndividtypeAftaletype(getDato());
		}

		return individtypeaftaletyper;
	}

	/**
	 * @return krævede individtyper for alle relationstyper denne forsikringstype
	 */
	public Individtype[] getIndividtyperRelationer() {
		Set<Individtype> set = new HashSet<>();
		IntpAftp[] individtypeAftaletyper = getIndividtypeAftaletyper();
		if (individtypeAftaletyper != null) {
			for (IntpAftp rel : individtypeAftaletyper) {
				if (rel.getIntpAftpFhbsk().isMaegler() || rel.getIntpAftpFhbsk().getBenaevnelse().startsWith("Anden provision"))
					continue;
				set.add(rel.getIndividtype());
			}
		}
		Individtype[] result = new Individtype[set.size()];
		return set.toArray(result);
	}

	/**
	 * @return {@link AftaleBO#hasRelationer}
	 */
	public boolean hasRelationer() {
		// Vær sikker på at forholdsbeskrivelserne er loadet.
		getForholdsbeskrivelser();
		return hasRelationer;
	}

	/**
	 * Aftalens relationer.
	 */
	public List<RelationsBO> getRelationer() {

		if (relationer == null) {
			loadRelationer();
		}

		return relationer;
	}

	/**
	 * Forholdsbeskrivelserne findes ud fra hver enkelt individtype/aftaletype.
	 * <p>
	 * Hvis det er en ændringssituation finder funktionen først de
	 * forholdsbeskrivelser der er tilknyttet aftalen og dernæst findes de
	 * tilladte forholdsbeskrivelser ud fra regelsættet. Er det nyoprettelse
	 * findes kun til mulige.
	 */
	protected void loadRelationer() {
		relationer = new ArrayList<RelationsBO>();

		// *** Aktuelle relationer ***
		if (aftale != null) {
			IndividAftale[] inaf = null;
			if (!medtagFremtidige())
				inaf = aftale.getIndividAftale(false, getDato());
			else
				inaf = aftale.getIndividAftaleGldOgFremtidige(false, getDato());
			for (int i = 0; inaf != null && i < inaf.length; i++) {

				RelationsBO r = new RelationsBO(
						(RelationsHolderRelationIF) inaf[i],
						inaf[i].getIntpAftpFhbsk(),
						this, getDato());
				relationer.add(r);
			}
		}

		if (requestDocumentAsFacade_ != null) {
			if (requestDocumentAsFacade_.isNaersikring())
				return;
			// Helt irrelevante og forstyrrende.
		}

		// Mulige relationer er de individerne tilknyttet tegnesaf individet.
		Individ individ = getParent() != null ? (Individ) getParent().getEntitet() : null;
		if (individ != null) {
			Individ[] individFraforhold = ((IndividImpl) individ).
					getTilknyttedeIndividerTilVisningGld(getDato());

			RelationsBO.UniqueEntitetComparator comparator =
					new RelationsBO.UniqueEntitetComparator();

			for (int i = 0; i < individFraforhold.length; i++) {
				RelationsBO rbo = new RelationsBO(individFraforhold[i],
						null, this, getDato());

				if (ContainerUtil.indexOf(rbo, relationer, comparator) != 0) {
					relationer.add(rbo);
				}
			}
		}
	}

	/**
	 * Undersøger om aftaletypen kan tilknyttes prov.modtager.
	 * Bemærk: kaldes før save, kan altså ikke bruge save-metodernes logik - eller hva ?
	 */
	public boolean hasProvsatsPaaAftaletype() {
		return ((Aftaletype) type_).hasProvisionSatsRegel(getAftaleHaendelsestypeKorrigeret());
	}

	/**
	 * @return Hændelsestype Ændring / Nytegning / Nytegning hvis ændring pr. gld
	 */
	private String getAftaleHaendelsestypeKorrigeret() {

		if (getEntitet() == null)
			return Aftalehaendelse.HAENDELSESTYPE_NYTEGNING;

		if (getDato() != null && getEntitet().getGld().compareTo(getDato()) == 0)
			return Aftalehaendelse.HAENDELSESTYPE_NYTEGNING;
		// ændring pr. nytegning = nytegningshændelse

		return Aftalehaendelse.HAENDELSESTYPE_AENDRING;
	}

	/**
	 * @see AftaleBO#loadYdelser loadYdelser - kan genindlæse
	 */
	@Override
	public List<YdelseBO> getYdelser() {
		if (ydelser == null) {
			loadYdelser();
		}
		return ydelser;
	}

	/**
	 * @see dk.gensam.gaia.model.aftale.Aftale#getYdelsesAngivelser(BigDecimal)
	 * @see dk.gensam.gaia.model.aftale.Aftaletype#getYdelsestype(BigDecimal)
	 */
	protected void loadYdelser() throws DBException {
		if (ydelser == null) {

			ydelser = new ArrayList<YdelseBO>();
			AftalekompYdtpAngivelseIF[] ydelsesAngivelser = null;

			ArrayList ydelsestyper = new ArrayList();

			Ydelsestype[] yt = ((Aftaletype) getType()).getYdelsestype(getDato());
			if (yt != null && yt.length > 0) {
				ydelsestyper.addAll(Arrays.asList(yt));
			}

			if (getEntitet() != null) {
				if (!medtagFremtidige())
					ydelsesAngivelser = ((Aftale) getEntitet()).getYdelsesAngivelser(getDato());
				else
					ydelsesAngivelser = ((Aftale) getEntitet()).getYdelsesAngivelserGldOgFremtidige(getDato());
			}

			for (int i = 0; ydelsesAngivelser != null && i < ydelsesAngivelser.length; i++) {
				ydelser.add(new YdelseBO(ydelsesAngivelser[i], this, getDato()));
				ydelsestyper.remove(ydelsesAngivelser[i].getYdelsestype());
			}

			if (isNew() || adfaerdStrategy_ == null || adfaerdStrategy_.loadBOUselekterede()) {
				for (int i = 0; ydelsestyper != null && i < ydelsestyper.size(); i++) {
					ydelser.add(new YdelseBO((RegelsaetType) ydelsestyper.get(i), this, getDato()));
				}
			}
			Collections.sort(ydelser, YdelseBO.YDELSE_COMPARATOR);
		}
	}

	/**
	 * En overskrivning af metoden fra BusinessObject
	 */
	public void add(BusinessObject pChild) {
		if (pChild instanceof GenstandBO)
			add((GenstandBO) pChild);

		else if (pChild instanceof StatusBO) {
			aftaleStatustyper.add((StatusBO) pChild);

		} else if (pChild instanceof AdresseBO) {
			if (adresser == null)
				adresser = new ArrayList();
			adresser.add((AdresseBO) pChild);

		} else if (pChild instanceof KlausulBO) {
			klausuler.add((KlausulBO) pChild);

		} else if (pChild instanceof TilgangAfgangBO) {
			if (tilAfgangOplysninger == null)
				tilAfgangOplysninger = new ArrayList<TilgangAfgangBO>(1);
			tilAfgangOplysninger.add((TilgangAfgangBO) pChild);

		} else if (pChild instanceof FrekvensBO) {
			frekvens.add((FrekvensBO) pChild);

		} else if (pChild instanceof ForfaldBO) {
			forfald.add((ForfaldBO) pChild);

		} else if (pChild instanceof PBSTilmeldingBO) {
			pbsTilmelding = (PBSTilmeldingBO) pChild;
		}

	}

	/**
	 * @param pOpsigelsestype
	 */
	public void setTilgangsBO(Opsigelsestype pOpsigelsestype) {
		if (pOpsigelsestype != null) {
			if (tilAfgangOplysninger == null)
				tilAfgangOplysninger = new ArrayList<TilgangAfgangBO>(1);
			tilAfgangOplysninger.add(new TilgangAfgangBO(pOpsigelsestype, this, getDato(), null));
		}
	}

	/**
	 * @param pOpsigelsestype
	 * @return selekteret instans af TilgangAfgangBO, null hvis typen ikke findes eller ikke er loadet
	 */
	public TilgangAfgangBO setTilgangBOSelected(Opsigelsestype pOpsigelsestype) {
		List<TilgangAfgangBO> tilAfgangsOplysninger = getTilAfgangsOplysninger();
		for (TilgangAfgangBO tilgangAfgangBO : tilAfgangsOplysninger) {
			if (tilgangAfgangBO.getType().equals(pOpsigelsestype)) {
				tilgangAfgangBO.setSelecteret(true);
				return tilgangAfgangBO;
			}
		}
		return null;
	}

	/**
	 * Adder ubetinget YdelseBO til listen over BO'ets ydelser.
	 *
	 * @param pYdelseB0
	 */
	public boolean addYdelseBO(YdelseBO pYdelseB0) {
		if (ydelser == null)
			ydelser = new ArrayList<>();
		ydelser.add(pYdelseB0);
		return true;
	}

	/**
	 * Add Type hvis den ikke findes i forvejen
	 *
	 * @param pType
	 */
	public void addIfAbsent(RegelsaetType pType) {
		if (pType instanceof Ydelsestype) {
			if (ydelser != null) {
				for (int i = 0; i < ydelser.size(); i++) {
					if ((ydelser.get(i)).getType().equals(pType))
						return;
				}
				/**
				 * Instantier og add
				 *
				 */
				YdelseBO ny = new YdelseBO(pType, this, this.getDato());
				if (ny.getAngivelse() == null)
					ny.setAngivelseUdenValidering(GaiaConst.NULBD2dec);
				ydelser.add(ny);
			}
		}
	}

	/**
	 * En overskrivning af metoden fra BusinessObject
	 */
	public void remove(BusinessObject pChild) {
		if (pChild instanceof GenstandBO)
			remove((GenstandBO) pChild);

		else if (pChild instanceof StatusBO) {
			aftaleStatustyper.remove(pChild);

		} else if (pChild instanceof AdresseBO) {
			adresser.remove((AdresseBO) pChild);
		}
	}

	/**
	 * Bliver kaldt fra setSelection på GenstandBO
	 */
	public void add(GenstandBO pChild) {
		addGenstand((GenstandBO) pChild.clone());
	}

	public void addGenstand(GenstandBO pChild) {
		genstande.add(pChild);
	}

	/**
	 * Bliver kaldt fra setSelection på GenstandBO
	 */
	public void remove(GenstandBO pChild) {
		if (pChild.isGenstand()) {
			pChild.getFaellesGenstandsOplBO().removeGenstandBO(pChild);
		}
		int index = -1;
		for (int i = 0; i < genstande.size(); i++) {
			if (genstande.get(i).hashCode() == pChild.hashCode()) {
				index = i;
			}
		}
		if (index >= 0)
			genstande.remove(index);
	}

	/**
	 * Kald af <code>setSelecteret(true)</code> bruger denne funktion.
	 * <p>
	 *
	 * @see BusinessObject#setSelecteret(boolean)
	 */
	public void add(RelationsBO relation) {
		if (relationer == null)
			relationer = new ArrayList<RelationsBO>(1);

		this.relationer.add(relation);

	}

	/**
	 * Kald af <code>setSelecteret(false)</code> bruger denne funktion.
	 * <p>
	 *
	 * @see BusinessObject#setSelecteret(boolean)
	 */
	public void remove(RelationsBO relation) {
		/**
		 * DISABLET!!
		 * relationer.remove(relation);
		 */
	}

	/**
	 * Forholdsbeskrivelser tilknyttet aftaletypen via database relationen
	 * IndividtypeAftaletype <code>(INTPAFTP)</code>.
	 */
	public ModelObjekt[] getForholdsbeskrivelser() {

		if (forholdsbeskrivelser == null) {
			loadForholdsbeskrivelser();
		}

		return forholdsbeskrivelser;
	}

	/**
	 * Load af forholdsbeskrivelser vha. individtype-aftaletype.
	 * Mæglerbeskrivelser frasorteres og til sidst opdateres hasRelationer.
	 */
	protected void loadForholdsbeskrivelser() {
		IntpAftp[] intpAftp =
				((Aftaletype) getType()).getIndividtypeAftaletype(getDato());

		ArrayList result = new ArrayList();

		for (int i = 0; intpAftp != null && i < intpAftp.length; i++) {

			if (!intpAftp[i].getIntpAftpFhbsk().isMaegler()) {
				result.add(intpAftp[i].getIntpAftpFhbsk());
			}
		}

		forholdsbeskrivelser = (IntpAftpFhbsk[]) ContainerUtil.toArray(result);

		hasRelationer = forholdsbeskrivelser != null
				&& forholdsbeskrivelser.length > 0;
	}

	/**
	 * @see IntpAftpFhbskImpl#getTvungneIntpAftpsmhbsk(Type, BigDecimal)
	 */
	public ModelObjekt[] getTvungneForholdsbeskrivelser() {

		return IntpAftpFhbskImpl.
				getTvungneIntpAftpsmhbsk((Aftaletype) getType(), getDato());
	}

	/**
	 * Returner parent som et IndividBO.
	 */
	public IndividBO getIndivid() {
		return (IndividBO) getParent();
	}

	/**
	 * Tilladte adresser i følge regelsættet med undtagelse af pbs adresser.
	 *
	 * @return List med AdresseBO'er tilknyttet aftalen.
	 */
	public List<AdresseBO> getAdresser() {
		if (adresser == null) {
			loadAdresser();
		}
		return adresser;
	}

	public Adressetype[] getAdressetyper(BigDecimal dato) {
		return ((Aftaletype) getType()).getAdressetype(dato);
	}

	public boolean hasAdresser(BigDecimal dato) {
		Adressetype[] at = getAdressetyper(dato);
		return (at != null && at.length > 0);
	}

	public AdresseHoldertypeAdressetype[]
	getAdresseHoldertypeAdressetype(BigDecimal dato) {

		return ((Aftaletype) getType()).getAftaletypeAdressetype(dato);
	}

	public Individ getTegnesAf() {
		return getParent() != null ? (Individ) getParent().getEntitet() : null;
	}

	/**
	 * Tjekker om områdetypen "RISIKOSTED" findes, og om aftaletypen har en adressetype der starter med "REAS" i
	 * dens korte benævnelse benævnelse.
	 *
	 * @return svaret
	 */
	public boolean isRisikostedMulig() {

		OmraadetypeImpl type = OmraadetypeImpl.getRisikosted();
		if (type != null) {
			// Tjek aftaletype-adressetyper
			Adressetype[] adrTyper = getAdressetyper(getDato());
			if (adrTyper != null && adrTyper.length > 0) {
				for (int i = 0; i < adrTyper.length; i++) {
					Adressetype adtp = adrTyper[i];
					if (adtp != null) {
						if (adtp.isReasAdressetype()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	protected void loadAftaleOmraadeRisikosted() {
		RegelsaetType type = QueryService.lookupRegelType(OmraadetypeImpl.class, Omraadetype.RISIKOSTED, "omraadeforkortelse");
		if (type == null) {
			aftaleOmraadeRisikosted = new ArrayList<RisikostedBO>();
			return;
		}
		boolean konv = adfaerdStrategy_ != null &&
				adfaerdStrategy_.isGsproGsproKonvertering();

		RegelsaettypeRelation typeRel = new RegelsaettypeRelation(type, false);
		aftaleOmraadeRisikosted = new ArrayList();

		AfOrRisikosted afOrRist = null;
		AfOrRisikosted[] afOrRistFremtidige = null;

		if (getEntitet() != null) {
			if (!konv) {
				afOrRistFremtidige = ((Aftale) getEntitet()).getfremtidigeAfOrRisikosted(getDato());

				afOrRist = ((Aftale) getEntitet()).getAfOrRisikosted(getDato());
				if (afOrRist != null) {
					// Persistent data!
					aftaleOmraadeRisikosted.add(new RisikostedBO(afOrRist, typeRel, this, getDato(), afOrRistFremtidige, !konv));
				} else {
					// Ikke-persistent aftaleområderisikosted
					aftaleOmraadeRisikosted.add(new RisikostedBO(typeRel, this, getDato(), afOrRistFremtidige, !konv));
				}
			} else {
				AfOrRisikosted[] all = ((Aftale) getEntitet()).getAfOrRisikosted();
				if (all != null && all.length > 0) {
					for (AfOrRisikosted afOrRisikosted : all) {
						if (afOrRisikosted.isAnnulleret() || afOrRisikosted.isOphoert(getDato()))
							continue;
						aftaleOmraadeRisikosted.add(new RisikostedBO(afOrRisikosted, typeRel, this, getDato(), null, false));
					}
				}
			}
		} else {
			aftaleOmraadeRisikosted.add(new RisikostedBO(typeRel, this, getDato(), afOrRistFremtidige, !konv));
		}
	}

	/**
	 * Bygger instansvariabelliste over alle PBSDebitorGruppeNr på aftalen.
	 *
	 * @since GSPro 2.3
	 */
	public void loadAftalePBSDebitorGruppeNr() {
		aftalePBSDebitorGruppeNr = new ArrayList();
		if (!(getParent() instanceof PanthaverBO)) {
			PBSDebitorGruppeNr pbsDebGr = getPBSDebitorGruppeNrDefault();
			AftalePBSDebitorGruppeNr afPBSDebGr = null;
			AftalePBSDebitorGruppeNr[] afPBSDebGrFremtidige = null;

			if (getEntitet() != null) {
				BigDecimal dato = getDato();
				if (isKopi_ && getFraDato().compareTo(dato) > 0) {
					dato = getFraDato();
				}
				afPBSDebGrFremtidige = ((Aftale) getEntitet()).getAfPBSDebitorGruppeNrFremtidige(dato);
				if (!medtagFremtidige()) {
					afPBSDebGr = ((Aftale) getEntitet()).getAftalePBSDebitorGruppeNr(dato);
					if (afPBSDebGr != null) {
						// Persistent data!
						aftalePBSDebitorGruppeNr.add(new PbsDebitorgruppeBO(afPBSDebGr, this, dato, afPBSDebGrFremtidige));
					} else {
						// Ikke-persistent aftalePBSDebitorGruppeNr
						aftalePBSDebitorGruppeNr.add(new PbsDebitorgruppeBO(pbsDebGr, this, dato, afPBSDebGrFremtidige));
					}
				} else {
					AftalePBSDebitorGruppeNr[] afPBSDebGrer = ((Aftale) getEntitet()).getAfPBSDebitorGruppeNrGaeldendeOgFremtidige(dato);
					for (int i = 0; afPBSDebGrer != null && i < afPBSDebGrer.length; i++) {
						aftalePBSDebitorGruppeNr.add(new PbsDebitorgruppeBO(afPBSDebGrer[i], this, dato, afPBSDebGrFremtidige));
					}
				}
			} else {// Ikke-persistent aftalePBSDebitorGruppeNr
				aftalePBSDebitorGruppeNr.add(new PbsDebitorgruppeBO(pbsDebGr, this, getDato(), afPBSDebGrFremtidige));
			}
		}
	}

	protected void loadPBSTilmelding() {
		if (pbsTilmelding == null) {
			pbsTilmelding = new PBSTilmeldingBO(this, getDato());
		}
	}

	/**
	 * Default PBSDebitorGruppeNr findes i følgende rækkefølge
	 * 1. Aftale
	 * 2. Aftaletype
	 * 3. Individegenskab selskab
	 * 4. Selskab default
	 *
	 * @return PBSDebitorGruppeNr
	 * @since GSPro 2.3
	 */
	private PBSDebitorGruppeNr getPBSDebitorGruppeNrDefault() {
		//Aftale
		if (getEntitet() != null) {
			AftalePBSDebitorGruppeNr afPbsDebGr = ((Aftale) getEntitet()).getAftalePBSDebitorGruppeNr(getDato());
			if (afPbsDebGr != null) {
				return afPbsDebGr.getPBSDebitorGruppeNr();
			}
		}

		//Aftaletype
		Aftaletype aftp = (Aftaletype) getType();
		if (aftp != null) {
			AftaletypePBSDebitorGruppeNr atPbsDebGr = aftp.getAftaletypePBSDebitorGruppeNr(getDato());
			if (atPbsDebGr != null) {
				return atPbsDebGr.getPBSDebitorGruppeNr();
			}
		}

		if (getTegnesAf() != null) {
			//Individegenskab selskab (tilhørsforhold)
			String ieID = getTegnesAf().getSelskabID();
			Individegenskab ie = (Individegenskab) DBServer.getVbsfInst().lookup(IndividegenskabImpl.class, ieID);
			if (ie != null) {
				IndividegenskabPBSDebitorGruppeNr iePbsDebGr = ie.getIndividegenskabPBSDebitorGruppeNr(getDato());
				if (iePbsDebGr != null) {
					return iePbsDebGr.getPBSDebitorGruppeNr();
				}
			}
		}

		//Selskab default
		return PBSDebitorGruppeNrImpl.getPBSDebitorGruppeNrDefault();
	}

	/**
	 * @todo Implementer load af fremtidige adresser på en ordenlig måde!
	 * @see AftaleBO#getAdresser
	 */
	protected void loadAdresser() {
		// Tilladte adressetyper i forbindelse med denne aftaletype.
		ArrayList<Adressetype> tilladteAdressetyper = new ArrayList(ContainerUtil.asList(
				((Aftaletype) getType()).getAdressetype(getDato())));

        /* 4.10.2001 - Load af fremtidige adresser er lidt noget snavs... først hiver vi fat i alle
            fremtidige adresser, frasorter dem som ikke er 'lovlige' adressetyper. Hvis der er nogle
            'lovlige' fremtidige adresser tilbage, sættes boolean flag for at signalere der findes
            adresser i fremtid. Denne boolean sendes med til AdresseBO, som så finder ud af om metoden
            hasFremtidige skal returnere true eller false. Fremtidige adresser bygges op i et array af
            AftaleAdresser - det kunne være vi kan bruge dem på et tidspunk.. (men til hvad..??)
        */
		// Håndtering af fremtidige adresser:
		if (aftale != null) {
			AftaleAdresse[] afadFremtidige = (AftaleAdresse[]) aftale.getFremtidigeAftaleAdresser(getDato());
			ArrayList fremtidigeLovligeTyper_list = new ArrayList();
			if (afadFremtidige != null) {
				for (int f = 0; f < afadFremtidige.length; f++) {
					AftaleAdresse temp = afadFremtidige[f];
					if (temp != null) {
						if (!temp.getAdresse().getAdressetype().isPbsAdressetype() &&
								tilladteAdressetyper.contains(temp.getAdresse().getAdressetype())) {
							fremtidigeLovligeTyper_list.add(temp);
						}
					}
				}
			}

			fremtidigeLovligeAdresser = (AftaleAdresse[]) ContainerUtil.toArray(fremtidigeLovligeTyper_list);
			if (fremtidigeLovligeAdresser != null && fremtidigeLovligeAdresser.length > 0)
				hasFremtidigeAdresser_ = true;
			// END fremtidige adresser
		}
		if (isNew()) {
			adresser = new ArrayList<>();
			if (!(adfaerdStrategy_ != null &&
					adfaerdStrategy_.isGsproGsproKonvertering() &&
					tilladteAdressetyper.isEmpty())) {
				adresser.addAll(getTegnesAfAdresser());
			}
			if (adresser.isEmpty() && !tilladteAdressetyper.isEmpty()) {
				for (Adressetype tilladttype : tilladteAdressetyper) {
					if (tilladttype.isPbsAdressetype())
						continue;
					AdresseBO adbo = new AdresseBO(tilladttype, this, this.getDato());
					adresser.add(adbo);
				}
			}
			return;
		}
		AftaleAdresse[] afad = null;
		if (!medtagFremtidige())
			afad = aftale.getAftaleAdresse(getDato());
		else
			afad = aftale.getAftaleAdresseGldOgFremtidige(getDato());

		//Hvis een af adresserne er sat til ophør i fremtid skal fremtidsmarkering også sættes
		if (!hasFremtidigeAdresser_) {
			for (int i = 0; afad != null && i < afad.length; i++) {
				if (afad[i].isOphUdfyldt()) {
					hasFremtidigeAdresser_ = true;
					break;
				}
			}
		}

		adresser = new ArrayList<AdresseBO>();

		for (int i = 0; afad != null && i < afad.length; i++) {

			// Ingen pbs og Hvis tegnes af's adressetype er tilladt, så...!
			// Det burde kun være nødvendigt at frasortere pbs adresser, hvis
			// nogle adr. har ikke-tilladt type, hvad så?
			if (!afad[i].getAdresse().getAdressetype().isPbsAdressetype()
					&& tilladteAdressetyper.contains(afad[i].getAdresse().getAdressetype())) {

				AdresseBO adr = new AdresseBO(afad[i], this, getDato(),
						fremtidigeLovligeAdresser,
						hasFremtidigeAdresser_);
				adresser.add(adr);
//			} else {
//				log_.info("Adresser Ikke tilladt! "
//					+ afad[i].getAdresse().getAdressetype().getBenaevnelse());
			}
		}
		if (!(adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering())) {
			// Inkluder mulige de adresser der ikke allerede findes i adresserne.
			// Men det spilder vi ikke tiden på ved gsprogspro-konvertering
			List<AdresseBO> l = getTegnesAfAdresser();
			for (int i = 0; i < l.size(); i++) {

				if (!adresser.contains(l.get(i))) {
					adresser.add(l.get(i));
				}
			}
		}
	}

	/**
	 * Funktionen udnytter at Aftalens parent er et IndividBO. Adresserne med
	 * typer der ikke er tilladte for aftaletypen og pbs adresser er
	 * frasorteret.
	 *
	 * @return List med AdresseBO'er tilknyttet aftalen's tegnesaf individ.
	 */
	public List<AdresseBO> getTegnesAfAdresser() {
		if (tegnesAfAdresser == null) {
			loadTegnesAfAdresser();
		}

		return tegnesAfAdresser;
	}

	/**
	 * @see AftaleBO#getTegnesAfAdresser
	 */
	protected void loadTegnesAfAdresser() {

		if (getParent() == null || isSkipLoadIndividetsAdresser() || this.isPanthaveraftale()) {
			tegnesAfAdresser = new ArrayList<>(0);
			return;
		}

		List eksisterendeAdr = getAdresser();

		IndividEgenskabSystem ies = new IndividEgenskabSystem(
				(Individ) getParent().getEntitet(),
				EgenskabFactoryImpl.INDIVID);

		int antDageBagud = 0;
		if (adfaerdStrategy_ != null && adfaerdStrategy_.isGsproGsproKonvertering()) {
			antDageBagud = (1 * -366);
			// Vi får lidt flere adresser ved at kigge et år ekstra bagud. Finder altså IndividAdresse der er ophørt før Aftaleadresse.
		}

		AdresseEgenskabSystemComposite[] adrVis = !getParent().isNew() ? ies.getAdresserTilVisning(getDato(), antDageBagud) : null;

		if (adrVis != null) {
			Arrays.sort(adrVis, new AdresseEgenskabSystemTypeNavnSortering());
		}

		tegnesAfAdresser = new ArrayList<>();

		/**
		 * Tilladte adressetyper i forbindelse med denne aftaletype.
		 */
		ArrayList okTyper = new ArrayList(ContainerUtil.asList(
				((Aftaletype) getType()).getAdressetype(getDato())));

		/**
		 * Frasortering af pbs-adresser og adresser hvor tegnesAf's adressetype
		 * ikke er tilladt i forhold til INTP/AFTP.
		 */
		for (int i = 0; adrVis != null && i < adrVis.length; i++) {

			AdresseEgenskabSystemComposite aesc = adrVis[i];

			/**
			 * For hver tilladttype hentes adresse fra Adr-Composite't
			 */
			for (int count = 0; count < okTyper.size(); count++) {

				Adressetype adrtype = (Adressetype) okTyper.get(count);
				AdresseEgenskabSystem aes = aesc.get(adrtype);

				if (aes != null && !adrtype.isPbsAdressetype()) {
					if (DEBUG) debug("AftaleBO.loadTegnesAfAdresser, tilføjer "
							+ " adrBO med typen: " + adrtype.getBenaevnelse());

					AdresseBO adrBO = new AdresseBO(
							(Adresse) aes.getEgenskabHolder(),
							this, getDato(),
							fremtidigeLovligeAdresser,
							hasFremtidigeAdresser_);

					if (!eksisterendeAdr.contains(adrBO)) {
						tegnesAfAdresser.add(adrBO);
					}
				}
			}
		}
	}

	/**
	 * Funktionen bruges til at præsenterer det område der er tilknyttet
	 * aftalen. Bemærk! Området er kun tilgængeligt for persistente aftaler.
	 *
	 * @return Aftales område.getLabel for gemte aftaler. Ved nye aftaler
	 * returneres tom tekst streng.
	 */
	public String getOmraadeString() {
		if (aftale != null) {
			Omraade o = aftale.getOmraade(getDato());
			return (o != null ? o.getOmraadebenaevnelse() : "");
		}
		return "";
	}


	/**
	 * Til brug for OmrådeKonsekvens
	 */
	public boolean reasRisikostedFindes() {
		if (aftale != null) {
			return (aftale.getAfOrRisikosted(dato_) != null);
		}

		return false;
	}

	/**
	 * ...?
	 */
	public List<ForfaldBO> getForfald() {
		if (forfald == null) {
			loadForfald();
		}
		return forfald;
	}

	protected void loadForfald() {
		if (forfald == null) {

			forfald = new ArrayList<ForfaldBO>();
			forfaldFremtidige = new ArrayList<AftaleFftpMd>();
			ArrayList aftpFftp = null;


			if ((dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.IS_NY_PRIS_REGISTRERING_ANVENDES.isPresent() && isTariferet()) ||
					(!isTariferet() && (!(getEntitet() != null && (getEntitet().getGld().compareTo(getDato()) != 0)))) || isKopi()) {
//			if (	(!isTariferet() && (!(getEntitet() != null && (getEntitet().getGld().compareTo(getDato()) != 0)))) || isKopi()) {
				// hvis den er tariferet kan der ikke vælges nyt
				// bemærk: der loades relationer og ikke typeentiteter (som her er inderligt ligegyldig
				aftpFftp = new ArrayList(
						ContainerUtil.asList(((Aftaletype) getType()).getAftpFftp(getDato())));
			}
			AftaleFftpMd[] affmAlle = null;

			if (getEntitet() != null) {
				affmAlle = ((Aftale) getEntitet()).getAftaleFftpMdMedFremtidige(getDato());

//				if (affmAlle == null)
//					log_.info("AftaleBO loadForfald NULL NULL NULL ?");

				for (int i = 0; affmAlle != null && i < affmAlle.length; i++) {
					if (affmAlle[i] != null) {
//					log_.info("AftaleBO loadForfald " + affmAlle[i].getGld().toString());
						if (affmAlle[i].getGld().compareTo(dato_) > 0) {
							forfaldFremtidige.add(affmAlle[i]);
							if (medtagFremtidige())
								forfald.add(new ForfaldBO(affmAlle[i], this, getDato()));
						} else {
							forfald.add(new ForfaldBO(affmAlle[i], this, getDato()));
							//aftpFftp.remove(affmAlle[i]);
							// sådan kan man ikke gøre da tilladte og valgt ikke er af samme type og dermed aldrig
							// bliver equals
							// Vi må selv læse igennem og checke på månedsnummer
							AftpFftp w = null;
							for (int j = 0; aftpFftp != null && j < aftpFftp.size(); j++) {
								w = (AftpFftp) aftpFftp.get(j);
								if (w.getMaanedsnummeriaaret().compareTo(affmAlle[i].getMaanedsnummeriaaret()) == 0) {
									aftpFftp.remove(j);
									break;
								}
							}
						}
					}
				}
			}


			// De mulige der ikke allerede er tilføjet listen.
			if (aftpFftp != null) {
				for (Iterator iter = aftpFftp.iterator(); iter.hasNext(); ) {
					AftpFftp af = ((AftpFftp) iter.next());

					ForfaldBO forfaldBO = new ForfaldBO(af.getForfaldstype(),
							this, getDato());

					// NOT isValgbar => valgt!
					//forfaldBO.setSelecteret(!af.isValgbar());
					forfaldBO.setMaanede(af.getMaanedsnummeriaaret());

					forfald.add(forfaldBO);

				}
			}
		}
		Collections.sort(forfald);
		//log_.info("*** loadForfald: " + forfald + " ***");
	}

	public List<FrekvensBO> getFrekvens() {
		if (frekvens == null) {
			loadFrekvens();
		}
		return frekvens;
	}

	/**
	 * Giver den frekvens med den angivne kortbnv. De er defineret som strings i Frekvens.EN.MAANED mv...
	 *
	 * @param pFrekvensnavn
	 */
	public FrekvensBO getFrekvens(Frekvens.FREKVENSNAVNE pFrekvensnavn) {
		for (FrekvensBO frekvens : getFrekvens()) {
			if (frekvens.getFrekvensNavn() == pFrekvensnavn) {
				return frekvens;
			}
		}
		throw new GaiaRuntimeException("Der kunne ikke findes en frekvens med frekvensNavnet " + pFrekvensnavn.getKortbenaevnelse());
	}

	public boolean hasFrekvens() {
		getFrekvens();
		if (frekvens != null && frekvens.size() > 0)
			return true;
		return false;
	}

	protected void loadFrekvens() {
		if (frekvens == null) {

			frekvens = new ArrayList<FrekvensBO>();
			frekvensFremtidige = new ArrayList<AftaleFrekvens>();
			ArrayList frekvensstyper = null;

			frekvensstyper = new ArrayList(ContainerUtil.asList(((Aftaletype) getType()).getFrekvens(getDato())));

			AftaleFrekvens[] atfAlle = null;

			if (getEntitet() != null) {

				atfAlle = ((Aftale) getEntitet()).getAftaleFrekvensMedFremtidige(dato_);
//				if (atfAlle == null)
//					log_.info("AftaleBO loadfrekvens NULL NULL NULL ?????");
				for (int i = 0; atfAlle != null && i < atfAlle.length; i++) {
					if (atfAlle[i] != null) {
//					log_.info("AftaleBO loadfrekvens " + atfAlle[i].getGld().toString());

						if (atfAlle[i].getGld().compareTo(dato_) > 0) {
							frekvensFremtidige.add(atfAlle[i]);
							if (medtagFremtidige())
								frekvens.add(new FrekvensBO(atfAlle[i], this, getDato()));
						} else {
							dftFrekvensBO = new FrekvensBO(atfAlle[i], this, getDato());
							frekvens.add(dftFrekvensBO);

							if (frekvensstyper != null) {
								frekvensstyper.remove(atfAlle[i].getFrekvens());
							}
						}
					}
				}
			}

			for (int i = 0; frekvensstyper != null && i < frekvensstyper.size(); i++) {
				frekvens.add(new FrekvensBO((Frekvens) frekvensstyper.get(i), this, getDato()));
			}
		}
	}

	/**
	 * Brug en anden metode -- denne er specifikt lavet til Asgnm28267 og forsvinder igen
	 *
	 * @return
	 */
	public FrekvensBO getDftFrekvensBO() {
		loadFrekvens();
		return dftFrekvensBO;
	}

	/**
	 * @see dk.gensam.gaia.model.aftale#isTariferet()
	 */
	public boolean isTariferet() {
		return (aftale != null && !aftale.isTilbud() ? aftale.isTariferet() : false);
	}

	/**
	 * @see Aftale#isPBSTilmeldt(BigDecimal)
	 */
	public boolean isPBSTilmeldt(BigDecimal pDato) {
		return (aftale != null ? aftale.isPBSTilmeldt(pDato) : false);
	}

	public boolean isAnnulleret() {
		return isAnnulleret;
	}

	public boolean isNytTilbud() {
		return isNytTilbud;
	}

	public boolean isTilbud() {
		if (isNew()) {
			return isNytTilbud();
		} else {
			return getAftale().isTilbud();
		}
	}

	/**
	 * @return true hvis forsikring er under omtegning. Ellers false.
	 */
	public boolean isOmtegning(){
		return !isNew() && ((Aftale)getEntitet()).isTariferet();
	}

	/**
	 * @return <code>true</code> Hvis tilbud er godkendt. Bemærk at status Tilbud godkendt er endnu ikke persisteret
	 */
	public boolean isVedAtGodkendeTilbud() {
		return isVedAtGodkendeTilbud;
	}

	/**
	 * Næsten samme som isTilbud men svarer false hvis vi er ved at godkende et tilbud
	 *
	 * @return svaret
	 */
	public boolean isTilbudAtbehandleSom() {
		if (isNytTilbud || (!isVedAtGodkendeTilbud() && getAftale() != null && getAftale().isTilbud())) {
			return true;
		}
		return false;
	}

	/**
	 * @return <code>true</code> hvis forsikringen er en korttidsforsikring.
	 * @see AftaleEgenskabSystem#isKorttidAftale()
	 */
	public boolean isKorttidsAftale() {
		if(!isNew() && getAftaleEgenskabSystem() != null && !aftale.isGld(getAftaleEgenskabSystem().getAktuelDato())) { //AftaleEgenskabSystem er loaded pr. dagen før aftaleGældende
			return getAftale().isKorttid();
		}

		if (aftaleEgenskabSystem == null && !isNew()) {
			return getAftale().isKorttid();
		} else {
			return getAftaleEgenskabSystem().isKorttidAftale();
		}
	}

	/**
	 * Tjekke og markere om der er vognskift
	 */
	public void setVognskiftIgang() {
		vognskiftIgang_ = false;
		if (!isNew()) {
			List genstande = getGenstande();
			for (int i = 0; genstande != null && i < genstande.size(); i++) {
				GenstandBO gn = (GenstandBO) genstande.get(i);
				if (gn.isGenstand() && gn.isVognskiftIgang()) {
					vognskiftIgang_ = true;
				}
			}
		}
	}

	/**
	 * Hvis sættes udefra bruges den i stedet for defaults som Ændring og Nytegning
	 * Hvis arg == null saves ingen aftalehændelse ved save af aftalen
	 */
	public void setAftalehaendelsestypeAlternativ(String pAftalehaendelsestype) {
		aftalehaendelsestypeAlternativ_ = pAftalehaendelsestype;
		if (pAftalehaendelsestype == null) {
			skipSaveAftalehaendelse_ = true;
			setYdelsesBeregningEnabled(false);
		}

	}

	public void setAftalehaendelsestypeOgProvModtagere(String pAftalehaendelsestype, ProvisionsModtagerSaetHolder pSaet) {
		setAftalehaendelsestypeAlternativ(pAftalehaendelsestype);
		setProvSaetHack(pSaet);
	}

	/**
	 * I modsætning til getAftalehaendelsestypeKorrigeret returneres Ændring ved Ændring pr. nytegning
	 *
	 * @return Alternativ sat udefra, ellers Nytegning/Ændring
	 */
	private String getAftalehaendelsestypeTilSave() {
		if (aftalehaendelsestypeAlternativ_ != null)
			return aftalehaendelsestypeAlternativ_;
		else {
//    		log_.info("Aftalehændelsestype er ikke sat !!!!");
			if (!isNew())
				return Aftalehaendelse.HAENDELSESTYPE_AENDRING;
			else if (isNew())
				return Aftalehaendelse.HAENDELSESTYPE_NYTEGNING;
		}
		return null;
	}

	/**
	 * Anvendes kun hvis der ønskes en satsdato der afviger fra standardreglen som for nogle selskaber er dagsdato, for andre boets getDato
	 *
	 * @param pSatsdato
	 */
	public void setSatsDatoAlternativ(BigDecimal pSatsdato) {
		satsDatoAlternativ_ = pSatsdato;
	}

	private BigDecimal getSatsDato() {
		if (satsDatoAlternativ_ != null && satsDatoAlternativ_.intValue() > 0)
			return satsDatoAlternativ_;
		if (!DBServer.getInstance().getSelskabsOplysning().provisionsModtagerSatsDatoDagsdato()) {
			return getDato();
		}
		return Datobehandling.getDagsdatoBigD();
	}

	public Aftalehaendelse getAftalehaendelse() {
		return aftalehaendelseSaved_;
	}


	protected void debug(String msg) {
		log_.info("[AftaleBO] " + msg);
	}

	/**
	 * Validering af min/max antal adresser i forhold til aftaletypen.
	 * <p>
	 * Valideringen foretages med udgangspunkt i de min/max værdier der er
	 * specificeret i regelsæt relationen imellem Aftaletype/adressetype.
	 * <p>
	 * <B>Værdien 0 opfattes som ingen regel specificeret.</B>
	 * <p>
	 *
	 * @throws GensamValidateException Hvis bare én (første) adressetype der
	 *                                 ikke overholder reglerne. Dvs. valideringen ikke giver
	 *                                 fuldstændigt billede af om data er i orden.
	 * @see AdresseValidator
	 */
	public void validateAdresser() throws GensamValidateException {
		Validator validator = new AdresseValidator(this, adresser, getDato());
		validator.validate();
	}

	public void validateAdresseOphoer() throws GensamValidateException {
		Validator validator = new AdresseValidator(this, adresser, getDato());
		validator.infoValidate();
	}

	// Bonusrykningsdato
	public BigDecimal getNaesteBonusrykningsDato() {
		if (bonusrykningsDato_ == null) {
			bonusrykningsDato_ = new BonusrykningsDato(this);
		}

		return bonusrykningsDato_.getNaesteBonusrykningsDato();
	}

	/**
	 *  Svar på om der skal "reloadBonus" hvis Godkendt tilbud og der er noget galt med bonusrykningsdato.
	 *  Det kan skyldes at man ændrer hovedforfald samtidtig med Godkendt tilbud.
	 * @return true hvis der skal "reload lovligeBonusrykningsdatoer"
	 */
	public boolean isBonusrykningsdatoerSkalReloadesVedGodkendTilbud() {
		return (isVedAtGodkendeTilbud() && !isNaesteBonusrykningsDatoGyldigForNyAftale(new BonusrykningsDato(this).getNaesteBonusrykningsDato()));
	}

	/**
	 * Svarer på om NaesteBonusrykningsDato er en gyldig dato eller ej.
	 * Bemærk at der er to bonusprincip.
	 * Pt. er det kun HF Forsikring der har bonusprincip 2. Andre selskaber har bonusprincip 1, dvs. KunHovedforfald.
	 * @param pNaesteBonusrykningsDato
	 * @return true hvis NaesteBonusrykningsDato er en gyldig dato. Ellers false.
	 */
	private boolean isNaesteBonusrykningsDatoGyldigForNyAftale(BigDecimal pNaesteBonusrykningsDato){
		BigDecimal naesteHovedForfaldsDato = getHovedforfaldDatoForNyAftale();
		if(AlleTillaegsfunktionaliteter.IS_BONUSPRINCIP_KUN_HOVEDFORFALD.isPresent()) {
			return Datobehandling.getMaanedFraDato(pNaesteBonusrykningsDato) == Datobehandling.getMaanedFraDato(naesteHovedForfaldsDato);
		}else {
			int[] gyldigeForfaldMaaneder = getGyldigeForfaldMaanederUdfraHovedforfaldMaaned(Datobehandling.getMaanedFraDato(naesteHovedForfaldsDato));
			int maanedNummerFraNaesteBonusrykningsDato = Datobehandling.getMaanedFraDato(pNaesteBonusrykningsDato);
			for (int i = 0; i < gyldigeForfaldMaaneder.length; i++) {
				if (maanedNummerFraNaesteBonusrykningsDato == gyldigeForfaldMaaneder[i]){
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Find de gyldige forfaldmåneder udfra hovedforfaldmåned
	 * @param pHovedforfaldMaanedNr
	 * @return de gyldige forfaldmånednr.
	 */
	int[] getGyldigeForfaldMaanederUdfraHovedforfaldMaaned(int pHovedforfaldMaanedNr){
		FrekvensBO frekvensBOValgt = getFrekvensBOValgt();
		if (frekvensBOValgt == null) return null;
		int antalOpkraevningerOmAaret = frekvensBOValgt.getAntalMaanederIFrekvens();
		return getGyldigeForfaldMaaneder(pHovedforfaldMaanedNr, antalOpkraevningerOmAaret);
	}

	/**
	 * Find de gyldige forfaldmåneder udfra hovedforfaldmåned og antalOpkraevningerOmAaret
	 * @param pHovedforfaldMaanedNr
	 * @param pAntalOpkraevningerOmAaret
	 * @return de gyldige forfaldmånednr.
	 */
	int[] getGyldigeForfaldMaaneder(int pHovedforfaldMaanedNr, int pAntalOpkraevningerOmAaret) {
		int[] gyldigeMaanedNr = new int[pAntalOpkraevningerOmAaret];
		gyldigeMaanedNr[0] = pHovedforfaldMaanedNr;
		for (int i = 1; i < pAntalOpkraevningerOmAaret; i++) {
			gyldigeMaanedNr[i]  = gyldigeMaanedNr[i-1] + 12/pAntalOpkraevningerOmAaret;
			if (gyldigeMaanedNr[i] > 12){
				gyldigeMaanedNr[i] = gyldigeMaanedNr[i] - 12;
			}
		}
		return gyldigeMaanedNr;
	}

	public void setHovedforfaldFrekvensAendretAtBonusdato(boolean pAendret) {
        if (bonusrykningsDato_ == null) {
            bonusrykningsDato_ = new BonusrykningsDato(this);
        }
        bonusrykningsDato_.setHovedforfaldFrekvensAendret(pAendret);
    }

	public BigDecimal[] getNaermesteLovligeBonusrykningsdatoer(boolean reloadBonus) {
		if (bonusrykningsDato_ == null) {
			bonusrykningsDato_ = new BonusrykningsDato(this);
		}

		BigDecimal[] naermesteLovligeBonusrykningsdatoer = bonusrykningsDato_.getNaermesteLovligeBonusrykningsdatoer(reloadBonus);
		if (naermesteLovligeBonusrykningsdatoer == null) {
			if (this.getEntitet().isOphoert()) {
				// Vi skal kunne understøtte, at ophørte forsikringer med tilknyttet bonustrin kan ændres og gemmes.
				// Alene for at kunne passere validering af udfyldt næste bonusregulering, sætter vi eneste tilladte dato til næste hovedforfald
				// Kunne også være ældste fremtidige udførte bonusregopgave - eller bare en dato (?)
				Aftale ophoertAftale = (Aftale) getEntitet();
				BigDecimal findNaesteHovedForfaldsDato = ophoertAftale.findNaesteHovedForfaldsDato(getDato());
				if (findNaesteHovedForfaldsDato != null) {
					naermesteLovligeBonusrykningsdatoer = new BigDecimal[1];
					naermesteLovligeBonusrykningsdatoer[0] = findNaesteHovedForfaldsDato;
				}
			}
		}
		return naermesteLovligeBonusrykningsdatoer;
	}

	public void setNaesteBonusrykningsDato(BigDecimal pDato) throws GensamValidateException {
		bonusrykningsDato_.setNaesteBonusrykningsDato(pDato);
	}

	public void setNaesteBonusrykningsDatoUdenValidering(BigDecimal pDato) {
		bonusrykningsDato_.setNaesteBonusrykningsDatoFlytAftaleUdenValidering(pDato);
	}

    /**
     * Bemærk at det afgøres ud fra status på children og ikke ud fra hvad anvendelsesprogrammer har meddelt.
     * Ændret valg fra persisteret værdi og tilbage vil svare false.
     *
     * @return  true hvis this er ny eller der er ændret valg af Hovedforfald
     */
	public boolean isHovedforfaldSelectionChanged() {
	    if (isNew())
	        return true;
        final List<ForfaldBO> forfalds = getForfald();
        if (forfalds != null) {
            for (ForfaldBO hvff : forfalds){
                if (hvff.isSelecteret() && hvff.isNew())
                    return true;
            }
        }
        return false;
    }

    /**
     * Bemærk at det afgøres ud fra status på children og ikke ud fra hvad anvendelsesprogrammer har meddelt.
     * Ændret valg fra persisteret værdi og tilbage vil svare false.
     *
     * @return  true hvis this er ny eller der er ændret valg af Frekvens
     */
	public boolean isFrekvensSelectionChanged() {
	    if (isNew())
	        return true;
        final List<FrekvensBO> frekvenser = getFrekvens();
        if (frekvenser != null) {
            for (FrekvensBO aFrekvens : frekvenser){
                if (aFrekvens.isSelecteret() && aFrekvens.isNew())
                    return true;
            }
        }
        return false;
    }

	/**
	 * Før godkendt tilbage i this som er den oprindelige forsikring.
	 *
	 * @param pAftaleBOFra
	 * @param pPbsAftalenummerKopieres
	 */
	public void opdater(AftaleBO pAftaleBOFra, boolean pPbsAftalenummerKopieres) {
		// Genstande
		opdaterGenstande(pAftaleBOFra);
		// Klausuler
		synkroniserKlausulBoListe(getKlausuler(), pAftaleBOFra.getKlausuler());
		// Rabatter
		synkroniserBoListe(getRabatter(), pAftaleBOFra.getRabatter());
		// Ydelser
		synkroniserYdelseBoListe(getYdelser(), pAftaleBOFra.getYdelser());
		// Egenskaber
		EgenskabSystem egenskabSystem = getEgenskabSystem();
		egenskabSystem.setPbsAftalenummerKopieres(pPbsAftalenummerKopieres);
		egenskabSystem.opdater(pAftaleBOFra.getEgenskabSystem());
		// Relationer
		synkroniserRelationerBoListe(this, getRelationer(), pAftaleBOFra.getRelationer());
		// Adresser
		synkroniserAdresserBOListe(this, getAdresser(), pAftaleBOFra.getAdresser());
		// Provisionsmodtagere
		synkroniserProvisionsmodtager(getNyesteIndividProvisionsModtagerSaetHolder().getBoListe(), pAftaleBOFra.getNyesteIndividProvisionsModtagerSaetHolder().getBoListe());
		// AftaleAfregningEjSamles
		synkroniserAftaleAfregningEjSamles(getAftaleAfregningEjSamles(), pAftaleBOFra.getAftaleAfregningEjSamles());
		// Minimumspræmie
		synkroniserMinimumPraemie(getMinPraemie(), pAftaleBOFra.getMinPraemie());
		// AftalePBSDebitorGruppeNr
		synkroniserAftalePBSDebitorGruppeNr(getAftalePBSDebitorGruppeNr(), pAftaleBOFra.getAftalePBSDebitorGruppeNr());
		// TilAfgangsOplysninger
		synkroniserTilAfgangsOplysninger(pAftaleBOFra);
		// PbsTlimelding
		synkroniserPbsTilmelding(getPBSTilmelding(), pAftaleBOFra.getPBSTilmelding());
		// Totalkundestatus
		overfoerTotalkundestatus(pAftaleBOFra);
		// Frekvens
		synkroniserFrekvens(getFrekvens(), pAftaleBOFra.getFrekvens());
		// Forfald
		synkroniserForfald(getForfald(), pAftaleBOFra.getForfald());
	}


	/**
	 * Asgnmt 25453 Him, Con vil alligevel have totalkundestatus overført ved godkendelse af tilbud.
	 *
	 * @param tilbudBo
	 */
	private void overfoerTotalkundestatus(AftaleBO tilbudBo) {
		Aftale tilbud = tilbudBo.getAftale();
		AftaleTotalkundetype aftaleTotalkundetypeTilbud = tilbud.getAftaleTotalkundetype(tilbudBo.getDato());
		if (aftaleTotalkundetypeTilbud != null && getAftale() != null) {
			AftaleTotalkundetype aftaleTotalkundetypeForsikring = getAftale().getAftaleTotalkundetype(tilbudBo.getDato());
			if (aftaleTotalkundetypeForsikring == null ||
					!aftaleTotalkundetypeForsikring.getTotalkundetype().equals(aftaleTotalkundetypeTilbud.getTotalkundetype())) {
				aftaleTotalkundetypeForsikring = (AftaleTotalkundetype) aftaleTotalkundetypeTilbud.cloneObject();
				aftaleTotalkundetypeForsikring.setAftale(getAftale());
				aftaleTotalkundetypeForsikring.oprettet();
				AftaleTotalkundetype[] alle = getAftale().getAftaleTotalkundetypeGldFremtidige(tilbudBo.getDato());
				PersistensService.save(aftaleTotalkundetypeForsikring, alle, new UpdateCacheable() {
					@Override
					public void removeFromCollection(ModelObjekt mo) {
						// autoremove
					}

					@Override
					public void addToCollection(ModelObjekt mo, ModelObjekt originalMO) {
						// autoadd
					}
				});

			}
		}
	}



	private void synkroniserPbsTilmelding(PBSTilmeldingBO pPbsTilmeldingTil, PBSTilmeldingBO pPbsTilmeldingFra) {
		List<AfregningstypeBO> afregningstyperTil = getAfregningstyper();
		for (AfregningstypeBO afregningstypeBO : afregningstyperTil) {
			if (afregningstypeBO.isSelecteret() && ((Afregningstype) afregningstypeBO.getType()).isPBS()) {
				return;
			}
		}
		if (pPbsTilmeldingFra.isSelecteret()) {
			pPbsTilmeldingTil.setSelecteret(true);
			pPbsTilmeldingTil.setAftaleTilmeldVia(pPbsTilmeldingFra.getAftaleTilmeldVia());
			pPbsTilmeldingTil.setKontonr(pPbsTilmeldingFra.getKontonr());
			pPbsTilmeldingTil.setRegnr(pPbsTilmeldingFra.getRegnr());
			setAfregningstypePBS(getDato());
		}
	}

	private void synkroniserTilAfgangsOplysninger(AftaleBO tilbudsBoet) {
		/**
		 * #26257. Tilogafgangsoplysninger flyttes fra tilbuddet as-is tilbage til forsikringen.<br>
		 *     Det antages, at de er født på tilbuddet og ikke kopieret med fra den oprindelige forsikring.
		 */
		Opsigelse[] opsigelseAlle = tilbudsBoet.getAftale().getOpsigelseAlle();
		List<TilgangAfgangBO> pTilAfgangsOplysningerTil = getTilAfgangsOplysninger();
		if (opsigelseAlle != null && pTilAfgangsOplysningerTil != null && pTilAfgangsOplysningerTil.size() > 0) {
			pTilAfgangsOplysningerTil.get(0).setAllOpsigelserDenneHolderHacked(opsigelseAlle);
		}
	}

	private void synkroniserAftalePBSDebitorGruppeNr(List<PbsDebitorgruppeBO> pAftalePBSDebitorGruppeNrTil, List<PbsDebitorgruppeBO> pAftalePBSDebitorGruppeNrFra) {
		PbsDebitorgruppeBO pbsDebitorgruppeBOTil = pAftalePBSDebitorGruppeNrTil.get(0);
		PbsDebitorgruppeBO pbsDebitorgruppeBOFra = pAftalePBSDebitorGruppeNrFra.get(0);
		pbsDebitorgruppeBOTil.setSelecteret(pbsDebitorgruppeBOFra.isSelecteret());
		if (pbsDebitorgruppeBOTil.isSelecteret()) {
			pbsDebitorgruppeBOTil.setPBSDebitorGruppeNrValgt(pbsDebitorgruppeBOFra.getPBSDebitorGruppeNrValgt());
		}
	}

	private void synkroniserMinimumPraemie(List<MinPraemieBO> pMinPraemieListeTil, List<MinPraemieBO> pMinPraemieListeFra) {
		if (pMinPraemieListeFra.size() > 0 && pMinPraemieListeTil.size() > 0) {
			MinPraemieBO minPraemieBOFra = pMinPraemieListeFra.get(0);
			MinPraemieBO minPraemieBOTil = pMinPraemieListeTil.get(0);
			minPraemieBOTil.setSelecteret(minPraemieBOFra.isSelecteret());
			if (minPraemieBOTil.isSelecteret()) {
				minPraemieBOTil.setBrugerbeloeb(minPraemieBOFra.getBeloebBruger());
			}
		}
	}


	private void synkroniserAftaleAfregningEjSamles(List<AftaleAfregningEjSamlesBO> pAftaleAfregningEjSamlesTil, List<AftaleAfregningEjSamlesBO> pAftaleAfregningEjSamlesFra) {
		pAftaleAfregningEjSamlesTil.get(0).setSelecteret(pAftaleAfregningEjSamlesFra.get(0).isSelecteret());
	}

	private void synkroniserProvisionsmodtager(List<ForsikringstagerAftalehaendelseProvModtBO> pProvisionsModtagerListeTil, List<ForsikringstagerAftalehaendelseProvModtBO> pProvisionsModtagerListeFra) {
		for (int i = 0; i < 3; i++) {
			ForsikringstagerAftalehaendelseProvModtBO forsikringstagerAftalehaendelseProvModtBOTil = pProvisionsModtagerListeTil.get(i);
			ForsikringstagerAftalehaendelseProvModtBO forsikringstagerAftalehaendelseProvModtBOFra = pProvisionsModtagerListeFra.get(i);
			forsikringstagerAftalehaendelseProvModtBOTil.setProvisionsModtager(forsikringstagerAftalehaendelseProvModtBOFra.getProvisionsModtager());
			forsikringstagerAftalehaendelseProvModtBOTil.setValgteKontakperson(forsikringstagerAftalehaendelseProvModtBOFra.getValgteKontaktperson());
		}
	}

	protected void synkroniserForfald(List<ForfaldBO> pForfaldListeTil, List<ForfaldBO> pForfaldListeFra) {
		for (ForfaldBO boTil : pForfaldListeTil) {
			for (ForfaldBO boFra : pForfaldListeFra) {
				if (boTil.getMaanednr().compareTo(boFra.getMaanednr()) == 0) {
					boTil.setSelecteret(boFra.isSelecteret());
					break;
				}
			}
		}
	}

	protected void synkroniserFrekvens(List<FrekvensBO> pFrekvensListeTil, List<FrekvensBO> pFrekvensListeFra) {
		for (FrekvensBO boTil : pFrekvensListeTil) {
			for (FrekvensBO boFra : pFrekvensListeFra) {
				if (boTil.getType().equals(boFra.getType())) {
					boTil.setSelecteret(boFra.isSelecteret());
					break;
				}
			}
		}
	}


	/**
	 * @param pAftaleBOFra - altså tilbuddet
	 */
	private void opdaterGenstande(AftaleBO pAftaleBOFra) {
		List<GenstandBO> behandledeGenstande = new ArrayList<>();
		List<GenstandBO> genstandeFra = pAftaleBOFra.getGenstande();
		// opdater eksisterende genstande
		for (GenstandBO genstandTil : getGenstande()) {
			if (genstandTil.isSelecteret()) {
				GenstandBO genstandFra = getGenstandBOFra(genstandTil, genstandeFra);
				if (genstandFra != null) {
					genstandTil.opdater(genstandFra, false);
					behandledeGenstande.add(genstandFra);
				} else {
					genstandTil.setSelecteret(false);
					genstandTil.opdater(null, false);
					behandledeGenstande.add(genstandTil);
				}
			}
		}
		// opdater nye genstande.
		for (GenstandBO genstandFra : genstandeFra) {
			if (genstandFra.isSelecteret() && !behandledeGenstande.contains(genstandFra)) {
				List<GenstandBO> genstandeTil = getGenstande();
				for (int i = 0; i < genstandeTil.size(); i++) {
					GenstandBO genstandTil = genstandeTil.get(i);
					if (genstandTil.isNew() && !genstandTil.isSelecteret() && !behandledeGenstande.contains(genstandFra) && genstandTil.getType().equals(genstandFra.getType())) {
						genstandTil.opdater(genstandFra, true);
						behandledeGenstande.add(genstandFra);
						behandelProduktskifte(genstandFra, genstandTil);
					}
				}
			}
		}
	}

	/**
	 * I praksis forudsætter anvendelse af denne metode, at this er et tilbud
	 *
	 * @return svaret på this indeholder genstande, der er mappet fra en genstand på en forsikring.
	 */
	public boolean containsProduktskifte() {
		GsproMapningService gsproMapningService = new GsproMapningService();
		List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
		for (GenstandBO gnbo : genstandeSelekteret) {
			// Find den oprindelige genstand til.
			Genstand genstandtil = gsproMapningService.getGenstandProdukskifteFra((Genstand) gnbo.getEntitet());
			if (genstandtil != null) {
				return true;
			}

		}
		return false;
	}

	private void behandelProduktskifte(GenstandBO pGenstandNyTilbud, GenstandBO pGenstandNy) {
		GsproMapningService gsproMapningService = new GsproMapningService();

		// Find den oprindelige genstand til.
		Genstand genstandtil = gsproMapningService.getGenstandProdukskifteFra((Genstand) pGenstandNyTilbud.getEntitet());
		if (genstandtil != null) {
			// så har der været et produktskifte
			gsproMapningService.setModelObjekt_(genstandtil);
			GsproMapning gsproMapning = gsproMapningService.getGsproMapningKunAktive();
			if (gsproMapning != null) {
                Genstand genstandFra = gsproMapning.getGenstandFra();
                GenstandBO genstandBOFra = getGenstandBOTilGenstand(genstandFra);
                ProduktskiftFactory produktskiftFactory = new ProduktskiftFactory(new ProduktskiftBonusBilTilFastImpl());
                produktskiftFactory.copyPanthaver(genstandBOFra, pGenstandNy, false);
                produktskiftFactory.copyFremtidigeOplysninger(genstandBOFra, pGenstandNy);
            } else {
			    log_.debug("Kunne ikke finde en mapning tilbage fra dig " + genstandtil.toString());
            }
		}

	}

	/**
	 * Denne metode er lavet som hjælpe metode til et produkt skifte, og må ikke anvendes til andet da den også udsøger/instantierer genstande der er sat til ophør dagen før aktuelle ændringsdato (getdato())
	 */
	private GenstandBO getGenstandBOTilGenstand(Genstand pGenstandFra) {
		List<GenstandBO> genstandeBOer = getGenstande();
		for (GenstandBO genstandBO : genstandeBOer) {
			if (genstandBO.getEntitet() != null && genstandBO.getEntitet().equals(pGenstandFra)) {
				return genstandBO;
			}
		}
		// ikke loaded pga af ophør.
		if(pGenstandFra.getOph().compareTo(BigDecimal.ZERO) > 0 && pGenstandFra.getOph().compareTo(Datobehandling.datoPlusMinusAntal(getDato(), -1)) == 0) {
			GenstandBO genstandBO = getGenstandBO(pGenstandFra.getGenstandsTypen());
			if(genstandBO != null) {
				GenstandBO tempGenstandBO = new GenstandBO(pGenstandFra.getEmne().getEmnetype(), pGenstandFra.getEmne(), new RegelsaettypeRelation(genstandBO.getType(), genstandBO.isTvunget()), pGenstandFra, genstandBO.flereEmnetyper_, genstandBO.getParent(), pGenstandFra.getOph(), genstandBO.getHovedprodukttype());
				tempGenstandBO.setFaellesGenstandsOplBO(genstandBO.getFaellesGenstandsOplBO());
				return tempGenstandBO;
			}
		}
		return null;
	}

	/**
	 * Udsøger det første og bedste genstandbo med en bestemt type.
	 * @param genstandsTypen
	 * @return et GenstandBO med der har samme type som den medsendte type hvis den findes, ellers null.
	 */
	private GenstandBO getGenstandBO(Genstandstype genstandsTypen) {
		List<GenstandBO> genstandeBOer = getGenstande();
		for (GenstandBO genstandBO : genstandeBOer) {
			if (genstandBO.getType().equals(genstandsTypen)) {
				return genstandBO;
			}
		}
		return null;
	}

	private GenstandBO getGenstandBOFra(GenstandBO genstandTil, List<GenstandBO> genstandeBOFra) {
		GsproMapningService gsproMapningService = new GsproMapningService();
		gsproMapningService.setModelObjekt_(genstandTil.getEntitet());
		GsproMapning gsproMapning = gsproMapningService.getGsproMapningKunAktive();
		if (gsproMapning != null && genstandeBOFra != null) {
			for (GenstandBO genstandBO : genstandeBOFra) {
				if (genstandBO.isSelecteret()) {
					if (genstandBO.getEntitet().getId().equals(gsproMapning.getIdentTil())) {
						return genstandBO;
					}
				}
			}
		}
		return null;
	}

	public void setForsikringsService(ForsikringsService forsikringsService) {
		this.forsikringsService = forsikringsService;
	}

	public ForsikringsService getForsikringsService() {
		return this.forsikringsService;
	}

	@Override
	public int getAntalDaekningerDerKræverPanthaver() {
		throw new UnsupportedOperationException("Det kan du ikke spørge et Aftalebo om - spørg genstanden ");
	}

	@Override
	public List<DaekningBO> getDaekninger() {
		return getAlleDaekninger(false);
	}

	public void setVognskiftIgangForTotalkunde(boolean vognskiftIgangForTotalkunde) {
		this.vognskiftIgangForTotalkunde = vognskiftIgangForTotalkunde;
	}

	/**
	 * Beregnet til at få de rigtige ydelsesvarieringsangivelse på ydelser efter man har opdateret et nyt aftalebo med setDatoAll().
	 * Metoden burde kaldes fra setDato, men her ved en fix i julen 2014 er man lidt af en kylling(har selv hotline ;-))
	 * Ved persistente aftaler bør indexreguleringen anvendes.
	 */
	public void synkroniserYdelsesangivelserTilKorrektVarieringsangivelse() {
		if (isNew()) {
			List<YdelseBO> ydelser = getAlleValgteYdelserUnderAftalen();
			if (ydelser != null) {
				for (YdelseBO ydelseBO : ydelser) {
					ydelseBO.synkroniserYdelsesangivelserTilKorrektVarieringsangivelse();
				}
			}
		}
	}

	/**
	 * @return set af ReaRisikotype med Områdetype Forsikringssted , empty hvis ingen - aldrig null
	 */
	public Set<ReaRisikoTp> collectReaRisikotyper() {
		Set<ReaRisikoTp> retur = new HashSet<>();
		final List<DaekningBO> daekningerSelekteredeDaekninger = getDaekningerSelekteredeDaekninger();
		if (daekningerSelekteredeDaekninger != null) {
			for (DaekningBO daekning : daekningerSelekteredeDaekninger) {
				final Produkttype type = (Produkttype) daekning.getType();
				final ReaRisikoTp[] reaRisikoTpGld = type.getReaRisikoTpGld(getDato());
				if (reaRisikoTpGld != null) {
					for (ReaRisikoTp rrtp : reaRisikoTpGld) {
						if (rrtp.isMedOmraadetypeForsikringssted(getDato()))
							retur.add(rrtp);
					}
				}
			}
		}
		return retur;
	}

	/**
	 * @return alle selekterede aftale- og genstandsadresser
	 */
	public List<AdresseBO> getAdresserAlleSelekterede() {
		List<AdresseBO> retur = new ArrayList<>();
		final List<AdresseBO> adresser1 = getAdresser();
		for (AdresseBO adr : adresser1) {
			if (adr.isSelecteret() && !retur.contains(adr))
				retur.add(adr);
		}
		final List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
		for (GenstandBO gn : genstandeSelekteret) {
			final List<AdresseBO> adresser2 = gn.getAdresser();
			for (AdresseBO adr : adresser2) {
				if (adr.isSelecteret() && !retur.contains(adr))
					retur.add(adr);
			}
		}
		return retur;
	}

	public String getSerialiseringsBenaevnelse(RegelsaetType pParentRegelsaetType, GSXMLBenaevnelseSerialiseringsKonfigurator pBenaevnelseKonfigurator) {
		String dataBetinget = getDatabetingetForsikringstypeBenaevnelse();
		if (dataBetinget != null)
			return dataBetinget;
		return getType().getSerialiseringsBenaevnelse(pParentRegelsaetType, pBenaevnelseKonfigurator);
	}

	/**
	 * Asgmt 26660 , 28944
	 *
	 * @return en selskabsbestemt databetinget formatering af aftalens forsikringstypebenævnelse.
	 */
	public String getDatabetingetForsikringstypeBenaevnelse() {
		String selskab = DBServer.getInstance().getDatabase();
		if (selskab.startsWith("FY") || selskab.startsWith("HI")) {
			String forstype2 = null;
			List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
			if (genstandeSelekteret == null)
			    genstandeSelekteret = new ArrayList<>(0);
			for (GenstandBO gnbo : genstandeSelekteret) {
				String genstandstypeKortBenaevnelse = gnbo.getType().getKortBenaevnelse();
				if (genstandstypeKortBenaevnelse.startsWith("BILTOT14"))
					forstype2 = "Bil-Total";
				if (genstandstypeKortBenaevnelse.startsWith("BILTOERH"))
					forstype2 = Aftaletype.CON_AFTALETYPE_BENAEVNELSE_BIL_ERHVERV_TOTAL;
				if (genstandstypeKortBenaevnelse.startsWith("BILTRIN14"))
					forstype2 = "Bil";
				if (genstandstypeKortBenaevnelse.startsWith("BILTRERH"))
					forstype2 = Aftaletype.CON_AFTALETYPE_BENAEVNELSE_BIL_ERHVERV_TRIN;
				if (genstandstypeKortBenaevnelse.startsWith("VARTOERH"))
					forstype2 = "Varebil-Total";
				if (genstandstypeKortBenaevnelse.startsWith("VARTRERH"))
					forstype2 = "Varebil";
				if (genstandstypeKortBenaevnelse.startsWith("INDBO14")) {
					final EgenskabSammenhaenge egenskabSammenhaeng = gnbo.getEgenskabSystem().getEgenskabSammenhaeng(Genstandsegngrp.Genstandsfelt.TEGNINGSTYPE.getKortBenaevnelse());
					forstype2 = egenskabSammenhaeng != null ? egenskabSammenhaeng.getIndtastetVaerdi() : null;
				}
				if (genstandstypeKortBenaevnelse.startsWith(Genstandstype.HIM_CON_GNTP_LANDBRUGSANSVAR))
					return "Ansvarsforsikring landbrug";
				if (genstandstypeKortBenaevnelse.startsWith(Genstandstype.HIM_CON_GNTP_ERHVERVSANSVAR))
					return "Erhvervsansvarsforsikring";
				if (this.getForsikringsType().equals(Forsikringstype.HPO)) {
					String genstandstypeBenaevnelse = gnbo.getType().getEksternBenaevnelse(null);
					return genstandstypeBenaevnelse + " hovedpolice";
				}
			}
			if (forstype2 != null) {
				String forstype1;
				if (selskab.startsWith("FY")) {
					final EgenskabSammenhaenge egenskabSammenhaeng = getIndivid().getEgenskabSystem().getEgenskabSammenhaeng(Individegenskabsgrp.Individfelt.TILHFORHe.getKortBenaevnelse());
					forstype1 = "Concordia";
					if (egenskabSammenhaeng != null && egenskabSammenhaeng.getIndtastetVaerdi() != null) {
						if (egenskabSammenhaeng.getIndtastetVaerdi().startsWith("Ærø"))
							forstype1 = "Ærø Brand";
						if (egenskabSammenhaeng.getIndtastetVaerdi().startsWith("Læsø"))
							forstype1 = "Læsø Brand";
					}
				} else {
					forstype1 = "Himmerland";
				}
				return forstype1 + " " + forstype2;
			}
		}
		if (selskab.startsWith("HF")) {
			final List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
			for (GenstandBO gnbo : genstandeSelekteret) {
				String kortBenaevnelse = gnbo.getType().getKortBenaevnelse();
				if (kortBenaevnelse.startsWith("FORSSENIOR")) {
					return "Seniorulykkesforsikring";
				}
				if (kortBenaevnelse.startsWith("PÅHCAMP.VG")) {
					return "Forsikring for campingvogn";
				}
				if (kortBenaevnelse.startsWith("PÅHTELTVG")) {
					return "Forsikring for Teltvogn";
				}
				if (kortBenaevnelse.startsWith("PÅHTRAILER")) {
					return "Forsikring for Trailer";
				}
				if (kortBenaevnelse.trim().compareTo(Genstandstype.HF_GNTP_LANDBRUGSANSVAR) == 0) {
					return "Landbrugsansvarsforsikring";
				}
				if (kortBenaevnelse.trim().compareTo(Genstandstype.HF_GNTP_VIRKSOMHEDSFORSIKRING) == 0) {
					return "Virksomhedsforsikring";
				}
			}
		}
		if (selskab.startsWith("GA")) {
			final List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
			for (GenstandBO gnbo : genstandeSelekteret) {
				String kortBenaevnelse = gnbo.getType().getKortBenaevnelse();
				if (kortBenaevnelse.startsWith(Genstandstype.CON_GNTP_CAMPINGVOGN)) {
					return "Campingvognsforsikring";
				}
				if (kortBenaevnelse.startsWith(Genstandstype.GAF_GNTP_SEL_TRAKTOR)) {
					return "Traktorforsikring";
				}
				if (kortBenaevnelse.startsWith(Genstandstype.GAF_GNTP_ULY_VOKSEN) || kortBenaevnelse.startsWith(Genstandstype.GAF_GNTP_ULY_BARN)) {
					return "Privat Ulykkesforsikring";
				}
				if (kortBenaevnelse.trim().compareTo(Genstandstype.GAF_GNTP_ATV_MOTORCROSSER) == 0) {
					return "ATV/Motocross-forsikring";
				}
				if (kortBenaevnelse.trim().compareTo(Genstandstype.GAF_GNTP_BIL_TRINREGULERET) == 0) {
					return "Personbilforsikring";
				}
			}
		}
		if (selskab.startsWith("GL")){
			final List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
			for (GenstandBO gnbo : genstandeSelekteret) {
				if (this.getForsikringsType().equals(Forsikringstype.HOV)) {
					String genstandstypeBenaevnelse = gnbo.getType().getEksternBenaevnelse(null);
					return genstandstypeBenaevnelse + " hovedpolice";
				}
			}
		}
		if (new DBServerUtil().isSelskabPresent(Selskaber.ET)) {
			DatabetingetForsikringstypeBenaevnelseEtu databetingetForsikringstypeBenaevnelseEtu =
					new DatabetingetForsikringstypeBenaevnelseEtu(this);
			return databetingetForsikringstypeBenaevnelseEtu.getDatabetingetForsikringstypeBenaevnelse();
		}

		return null;
	}

	/**
	 * Find et genstandsbo der ikke er selekteret og med den rigtige type og selekterer det
	 */
	public GenstandBO getUselekteretGenstandBO(String pGenstandstypeKortbenaevnelse) {
		GenstandBO newGenstandBO = null;
		List<GenstandBO> genstande = getGenstande();
		for (GenstandBO genstandBO : genstande) {
			if (!genstandBO.isSelecteret()) {
				Genstandstype genstandstype = (Genstandstype) genstandBO.getType();
				if (pGenstandstypeKortbenaevnelse.equals(genstandstype.getKortBenaevnelse())) {
					newGenstandBO = genstandBO;
				}
			}
		}
		if (newGenstandBO != null) {
			boolean disabled = newGenstandBO.getAftaleBO().getSammenhaengsregelHelper().setEventHandlerenDisabled();
			newGenstandBO.setSelecteret(true);
			if (disabled)
				newGenstandBO.getAftaleBO().getSammenhaengsregelHelper().setEventHandlerenEnabled(newGenstandBO.getParent(), newGenstandBO);
		}
		return newGenstandBO;
	}

	// ###############################
	// # HJÆLPE-KLASSER STARTER HER! #
	// ###############################

	/**
	 * Hjælpe klasse der anvendes til load af GenstandBO
	 */
	class EmneGenstandTypeWrapper {

		protected RegelsaettypeRelation emnetype_;
		protected Emne emne_;
		protected RegelsaettypeRelation genstandstype_;
		protected boolean findesGenstandstypePaaAndreEmnetyper_;
		protected Hovedprodukttype hovedprodukttype_;

		public EmneGenstandTypeWrapper(RegelsaettypeRelation pEmnetype, Hovedprodukttype pHovedprodukttype) {
			emnetype_ = pEmnetype;
			genstandstype_ = null;
			emne_ = null;
			findesGenstandstypePaaAndreEmnetyper_ = false;
			hovedprodukttype_ = pHovedprodukttype;
		}

		public EmneGenstandTypeWrapper(RegelsaettypeRelation pEmnetype, RegelsaettypeRelation pGenstandstype, Hovedprodukttype pHovedprodukttype) {
			emnetype_ = pEmnetype;
			// der undersøges lige om der er tilknyttet et emne af emnetypen, hvis der er skal der ikke oprettes et nyt.
			emne_ = null;
			if (!isNew())
				emne_ = ((Aftale) getEntitet()).getEmneGld((Emnetype) pEmnetype.getRegelsaetType(), getDato());
			genstandstype_ = pGenstandstype;
			findesGenstandstypePaaAndreEmnetyper_ = false;
			hovedprodukttype_ = pHovedprodukttype;
		}

		public RegelsaettypeRelation getEmnetype() {
			return emnetype_;
		}

		public Emne getEmne() {
			return emne_;
		}

		public RegelsaettypeRelation getGenstandstype() {
			return genstandstype_;
		}

		public Hovedprodukttype getHovedprodukttype() {
			return hovedprodukttype_;
		}


		public void setFindesGenstandstypePaaAndreEmnetyper(boolean pBoolean) {
			findesGenstandstypePaaAndreEmnetyper_ = pBoolean;
		}

		public boolean isGenstand() {
			return genstandstype_ != null;
		}


		public boolean equals(Object obj) {
			if (obj instanceof EmneGenstandWrapper) {
				if ((getEmnetype() == null) || (((EmneGenstandWrapper) obj).getEmnetype() == null) ||
						(!getEmnetype().equals(((EmneGenstandWrapper) obj).getEmnetype())))
					return false;
				if ((getGenstandstype() != null) || (((EmneGenstandWrapper) obj).getGenstandstype() != null))
					if ((getGenstandstype() == null) || (((EmneGenstandWrapper) obj).getGenstandstype() == null) ||
							(!getGenstandstype().equals(((EmneGenstandWrapper) obj).getGenstandstype())))
						return false;


				return true;
			}
			return false;
		}
	}

	/**
	 * Hjælpe klasse der anvendes til load af GenstandBO
	 */
	class EmneGenstandWrapper {

		protected Emne emne_;
		protected Genstand genstand_;
		protected EmneGenstandTypeWrapper type_;

		public EmneGenstandWrapper(Emne pEmne, EmneGenstandTypeWrapper pTypewrapper) {
			emne_ = pEmne;
			genstand_ = null;
			type_ = pTypewrapper;
		}

		public EmneGenstandWrapper(Emne pEmne, Genstand pGenstand, EmneGenstandTypeWrapper pTypewrapper) {
			emne_ = pEmne;
			genstand_ = pGenstand;
			type_ = pTypewrapper;
		}

		public RegelsaettypeRelation getEmnetype() {
			return type_.getEmnetype();
		}

		public RegelsaettypeRelation getGenstandstype() {
			return type_.getGenstandstype();
		}

		public Emne getEmne() {
			return emne_;
		}

		public Genstand getGenstand() {
			return genstand_;
		}

		public boolean isGenstand() {
			return genstand_ != null;
		}

		public EmneGenstandTypeWrapper getEmneGenstandTypeWrapper() {
			return type_;
		}
	}

	/**
	 *
	 */
	public void addDaekningTilAnnulleretListe(DaekningBO pDaekningBO) {
		if (daekningerRegAnnul == null)
			daekningerRegAnnul = new ArrayList<>();
		if (!daekningerRegAnnul.contains(pDaekningBO)) {
			daekningerRegAnnul.add(pDaekningBO);
		}
	}

	/**
	 * Opsamling af dækningsophør fra denne forsikringsændring
	 *
	 * @param pDaekningBO
	 */
	public void addDaekningTilOphoerListe(DaekningBO pDaekningBO) {
		if (daekningerRegOphoer == null)
			daekningerRegOphoer = new ArrayList<>();
		if (!daekningerRegOphoer.contains(pDaekningBO)) {
			daekningerRegOphoer.add(pDaekningBO);
		}
	}

	/**
	 * Opsamling af alle dækningsophør fra denne forsikringsændring som medfører fysisk sletning af dækning
	 *
	 * @param pDaekningBO
	 */
	public void addDaekningTilSletning(DaekningBO pDaekningBO) {
		if (daekningerRegSletning_ == null)
			daekningerRegSletning_ = new ArrayList<>();
		if (!daekningerRegSletning_.contains(pDaekningBO)) {
			daekningerRegSletning_.add(pDaekningBO);
		}
	}

	/**
	 * Opsamling af valgte dækninger fra denne forsikringsændring
	 *
	 * @param pDaekningBO
	 */
	public void addDaekningTilNyeListe(DaekningBO pDaekningBO) {
		if (this.getEntitet() == null)
			return;
		if (daekningerRegNye == null)
			daekningerRegNye = new ArrayList<>();
		if (!daekningerRegNye.contains(pDaekningBO)) {
			daekningerRegNye.add(pDaekningBO);
		}
	}

	/**
	 *
	 */
	public void oprydAnnullerOphoerDaekningLister() {
		daekningerRegAnnul = null;
		daekningerRegOphoer = null;
		daekningerRegSletning_ = null;
	}

	public ArrayList<DaekningBO> getAnnulleredeDaekningerListe() {
		return daekningerRegAnnul;
	}

	public ArrayList<DaekningBO> getOphoerteDaekningerListe() {
		return daekningerRegOphoer;
	}

	public ArrayList<DaekningBO> getOphoerteDaekningerSlettet() {
		return daekningerRegSletning_;
	}

	/**
	 * Load (første gang) og aflever model til fritekst på afregning.
	 */
	public synchronized AfregningFritekstModel getAfregningFritekstModel() {
		if (afregningFritekstModel == null) {
			afregningFritekstModel = new AfregningFritekstModel(this, dato_);
		}
		return afregningFritekstModel;
	}

	/**
	 * Load af afregning fritekstmodellen på bestemt dato. Er en model allerede
	 * loadet for en anden dato bliver den smidt ud og en ny oprettes.
	 *
	 * @param dato != <code>null</code>.
	 */
	public AfregningFritekstModel getAfregningFritekstModel(BigDecimal dato) {

		if (afregningFritekstModel == null
				|| dato_ == null
				|| dato.intValue() != dato_.intValue()) {

			afregningFritekstModel = new AfregningFritekstModel(this, dato);
		}

		return afregningFritekstModel;
	}

	public Aftale getAftale() {
		return aftale;
	}

	public boolean setAfregningstypePBS() {
		return setAfregningstypePBS(dato_);
	}

	public boolean setAfregningstypePBS(BigDecimal enDato) {
		List<AfregningstypeBO> artp = getAfregningstyper();
		RegelsaettypeRelation[] typer = ((Aftaletype) getType()).getAfregningstypeRelation(enDato);
		AftaleArtp[] fremtidigeArtp = aftale != null ? aftale.getAftaleArtpFremtidige(enDato) : null;
		AftaleArtp af = aftale != null ? aftale.getAftaleArtp(enDato) : null;
		if (af != null) {
			artp.add(new AfregningstypeBO(af, this, enDato, fremtidigeArtp));
			if (fremtidigeArtp != null) {
				AftaleArtp[] temp = new AftaleArtp[fremtidigeArtp.length + 1];
				temp[0] = af;
				for (int i = 1; i < fremtidigeArtp.length + 1; i++) {
					temp[i] = fremtidigeArtp[i - 1];
				}
				fremtidigeArtp = temp;

			} else {
				AftaleArtp[] temp = new AftaleArtp[1];
				temp[0] = af;
				fremtidigeArtp = temp;
			}

			boolean findesNewPBSAfregningstype = false;
			for (int i = 0; artp != null && i < artp.size(); i++) {
				if (artp.get(i).isAfregningstypePBS()) {
					findesNewPBSAfregningstype = true;
				}
			}

			// Hvis afregningstypen er GIROKORT - dvs ikke PBS, skal PBS tilføjes så der er mulighed for at vælge den
			if (!(findesNewPBSAfregningstype) && !(af.getAfregningstype().getKortBenaevnelse().trim().equals(Afregningstype.AFREGNINGSTYPE_PBS))) {
				if (typer != null) {
					for (int i = 0; i < typer.length; i++) {
						if (typer[i].getRegelsaetType().getKortBenaevnelse().trim().equals(Afregningstype.AFREGNINGSTYPE_PBS)) {
							artp.add(new AfregningstypeBO(typer[i].getRegelsaetType(), this, typer[i], getDato(), fremtidigeArtp));
						}
					}
				}
			}
		}

		if (artp != null) {
			for (AfregningstypeBO artpBO : artp) {
				if (artpBO.isSelecteret()) {
					if (artpBO.isAfregningstypePBS())
						return false;
					artpBO.setSelecteret(false);
				} else {
					if (artpBO.isAfregningstypePBS()) {
						artpBO.setSelecteret(true);
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Ændre afregningstypen PBS til giro.
	 *
	 * @return true hvis PBS ændres til Girokort
	 */
	public boolean setAfregningstypeGiro() {
		Aftale tmpAftale = getAftale();
		setMedtagFremtidige(false);
		loadAfregningstyper();
		if (tmpAftale != null && (!tmpAftale.isPBSEjTilmeldt(getDato()))) {
			for (int i = 0; afregningstyper != null && i < afregningstyper.size(); i++) {
				if (afregningstyper.get(i).isAfregningstypeGIRO()) {
					afregningstyper.get(i).setSelecteret(true);
				}
				if (!afregningstyper.get(i).isAfregningstypeGIRO() && afregningstyper.get(i).isSelecteret()) {
					afregningstyper.get(i).setSelecteret(false);
				}
			}
			return true;
		}

		// OK den aktuelle afregningstype er giro. Fjern evt. ophørs dato så fremtidige afregningstyper overskrives.
		for (int i = 0; afregningstyper != null && i < afregningstyper.size(); i++) {
			AfregningstypeBO artpbo = afregningstyper.get(i);
			if (artpbo.isSelecteret() && artpbo.isAfregningstypeGIRO() && artpbo.getTilDato().compareTo(BigDecimal.ZERO) != 0) {
				artpbo.setTilDato(BigDecimal.ZERO);
				return true;
			}
		}

		return false;
	}

	/**
	 * @return Tilladte Ydelsestyper på Emnetype pr. datoen
	 */
	public List getTilladteTyperChildren() {
		ArrayList ydelsestyper = new ArrayList();

		Ydelsestype[] yt = ((Aftaletype) getType()).getYdelsestype(getDato());
		if (yt != null && yt.length > 0) {
			ydelsestyper.addAll(ContainerUtil.asList(yt));
		}
		return ydelsestyper;
	}

	public boolean isStatusStoptype() {
		StatusBO[] aftaleStatus = getStatus();
		for (StatusBO status : aftaleStatus) {
			if (status.isSelecteret()) {
				return status.isStoptype();
			}
		}
		return false;
	}

	public boolean kanStoptypeSaettes() {
		StatusBO[] aftaleStatus = getStatus();
		for (StatusBO status : aftaleStatus) {
			if (status.isSelecteret()) {
				if (status.isStoptype() ||
						(status.getTilStatus() != null && status.getTilStatus().isStoptype())) {
					return true;
				}
			}
		}
		return false;
	}

	public void setStopType(boolean state, String pStatusTekst) {

		StatusBO[] aftaleStatus = getStatus();
		StatusBO status = aftaleStatus[0];

		if (status.isStoptype()) {
			if (!state) {
				status.setSelecteret(false);
				status.getTilStatus().setSelecteret(true);
				status.getTilStatus().setBrugerTekst(pStatusTekst);
				getAftaleStatustyper();
				this.add(status.getTilStatus());
			}
		} else {
			if (status.getTilStatus() != null) {
				status.setSelecteret(!state);
				status.getTilStatus().setSelecteret(state);
				if (state) {
					status.getTilStatus().setBrugerTekst(pStatusTekst);
				}
				// make this call to insure that this.aftaleStatustyper is initiallized
				getAftaleStatustyper();
				this.add(status.getTilStatus());


			}
		}


	}

	/**
	 * @return true hvis eksisterende Aftale entiten gemmes ved save.
	 */
	public boolean isGemEksisterendeAftaleEntitet() {
		return gemEksisterendeAftaleEntitet;
	}

	/**
	 * Giver mulighed for at sige til AftaleBO at evt. eksisterende aftale ikke skal gemmes.
	 *
	 * @param gemEksisterendeAftaleEntitet
	 */
	public void setGemEksisterendeAftaleEntitet(boolean gemEksisterendeAftaleEntitet) {
		this.gemEksisterendeAftaleEntitet = gemEksisterendeAftaleEntitet;
	}

	/**
	 * Initier AftaleBO og gem oplysninger nødvendige for håndtering af forlængelse af KorttidsForsikring.
	 *
	 * @param pFraDato           Forlængelse fra
	 * @param pTilDato           Forlængelse til
	 */
	public void initForlaengKorttid(BigDecimal pFraDato, BigDecimal pTilDato) {
		isForlaengKorttid = true;
		forlaengKorttidFraDato = pFraDato;
		forlaengKorttidTilDato = pTilDato;

		// Mestendels lånt/inspireret af NyIkraft & FlytAftale -- tilføjet de nødvendige manipulationer
		this.loadAllGaeldendeOgFremtidige();
		this.setTilDatoAll(BigDecimal.ZERO);
		forlaengKorttidGenstandeTilOphoer = new ArrayList<GenstandObjectIF>();
		List<GenstandBO> genstande = (List<GenstandBO>) this.getGenstande();
		for (GenstandBO gn : genstande) {
			if (gn.getEntitet() != null) {
				gn.getEntitet().setOph(this.getEntitet().getOph());
				forlaengKorttidGenstandeTilOphoer.add(((GenstandObjectIF) gn.getEntitet()));
				gn.clearPersistensAll();
			}
			gn.setMindsteFraDatoTestAll(this.getEntitet().getOph(), pFraDato);
		}
		this.setDatoAll(pFraDato);
		this.setTilDato(pTilDato);
		this.setMedtagFremtidige(false); // Der er alligevel intet fremtidigt
		this.loadAftaleEgenskabSystem();
		this.getEntitet().setOph(pTilDato);
	}

	public void setTotalkundeKontrolDatoVedFjernOphoer(BigDecimal pTotalkundeKontrolDatoVedFjernOphoer) {
		this.totalkundeKontrolDatoVedFjernOphoer = pTotalkundeKontrolDatoVedFjernOphoer;
	}

	public void setNyStatusTypeCallerTekst(String statusSkiftTekst) {
		nyStatusTypeCallerTekst = statusSkiftTekst;
	}

	public void setNyStatusTypeCaller(int caller) {
		nyStatusTypeCaller = caller;
	}

	public int getNyStatusTypeCaller() {
		return nyStatusTypeCaller;
	}

	@Override
	public boolean isMinimumLoadAfYdelser() {
		return minimumLoadAfYdelser;
	}

	@Override
	public void setMinimumLoadAfYdelser(boolean pMinimumLoadAfYdelser) {
		minimumLoadAfYdelser = pMinimumLoadAfYdelser;
	}

	/**
	 * Checker om der findes nogle dækninger med tilknyttede skadekrav med gældende >= pDato som har registrerede beløb != 0
	 *
	 * @see ProduktImpl#findesSkadekravMedBeloeb(BigDecimal)
	 * Hvis metoden returnere true, må aftalen ikke sættes til ophør.
	 */
	public boolean findesSkadekravMedBeloeb(BigDecimal pDato) {
		if (daekninger_ == null) {
			loadAlleDaekninger(this);
		}
		if (daekninger_ != null) {
			// For hver dækning, find skadekrav
			for (int d = 0; d < daekninger_.size(); d++) {
				if ((daekninger_.get(d)).findesSkadekravMedBeloeb(pDato)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checker om aftalens dækninger findes i ReasProduktOphoert hvor ReasProduktOphoert's fra-periode = pDato.
	 *
	 * @return true hvis der findes dækninger i ReadProduktOphoert med gældende = pDato.
	 */
	public boolean findesIReasProduktOphoer(BigDecimal pDato) {
		if (daekninger_ == null) {
			loadAlleDaekninger(this);
		}

		//List daekningerAlle = getAlleDaekninger(pAftaleBO);
		List<Produkt> daekningerGldFrem = Datobehandling.findGaeldendeOgFremtidige(daekninger_, pDato);
		if (daekningerGldFrem != null) {
			for (int d = 0; d < daekningerGldFrem.size(); d++) {
				if ((daekningerGldFrem.get(d)).findesIReasProduktOphoer(pDato)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Bygger instansvariabelliste over samtlige dækninger på aftalen.
	 */
	private void loadAlleDaekninger(AftaleBO pAftaleBO) {
		daekninger_ = new ArrayList<>();
		Produkt[] alleDaekninger = ((Aftale) pAftaleBO.getEntitet()).getDaekninger();
		if (alleDaekninger != null && alleDaekninger.length > 0) {
			daekninger_ = ContainerUtil.asList(alleDaekninger);
		}
	}

	// Liste med alle aftalens dækninger
	private List<Produkt> daekninger_ = null;


	public void udskrivOphoersBreve(BigDecimal pDato, Aftale pAftale, Individ pIndivid, boolean tophjaelp, boolean aarsrejse) {
		udskrivOphoersBreve(pDato, pAftale, pIndivid, tophjaelp, aarsrejse, false);

	}

	public void udskrivOphoersBreve(BigDecimal pDato, Aftale pAftale, Individ pIndivid, boolean tophjaelp, boolean aarsrejse, boolean ophørsdatoFjernet) {
		Individ individ = pAftale.getTegnesAfIndivid();
		PrintjobPersistensService pjps = null;
		if (GensamUtil.isRunningOnline() && dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.IS_RUNNING_TRANSFORM.isPresent()) {
			if (tophjaelp || aarsrejse || TotalkundetypeRegelManager.isDiplomOrdning()) {
				pjps = new PrintjobPersistensService(individ, "udskrivOphoersBreve for aftale.id: " + this.getAftale().getAftaleId(), pDato);
			}
		}

		if (tophjaelp) {
			NotesdokumenttypeImpl.tophjaelpBrev(pAftale, pDato, pjps);
		}
		if (aarsrejse) {
			NotesdokumenttypeImpl.aarsrejseForsikringsBrevVedOphoer(pAftale, pDato, pjps);
		}
		if (TotalkundetypeRegelManager.isTotalkundeSelskab()) {
			if (TotalkundetypeRegelManager.isDiplomOrdning()) {
				if (pAftale.isDiplomAftale(pDato)) {
					String brevtype = OverblikObjDiplom.DIPLOMOPHOER;
					if (ophørsdatoFjernet) {//ophørsdato er nu fjernet og aftalen er DiplomAftale pr. oprindelig ophørsdato så det betyder at der er
						// dannet et tilbagekaldesesbrev før --> vi danner lige et "tillykkebrev" igen.
						brevtype = OverblikObjDiplom.DIPLOMTILMELDANDET;
					}
					if (NotesdokumenttypeImpl.hasNotesdokumenttype(brevtype, pDato)) {
						Notesdokumenttype ndtp = NotesdokumenttypeImpl.loadNotesdokumenttype(brevtype, pDato);
						if (ndtp.danNotesDokument(pAftale, pDato, pjps)) {
							OverblikHaendelsestype obhntp = (OverblikHaendelsestype) OverblikHaendelsestypeImpl.getOverblikHaendelsestypeRegelAktiv(brevtype, pDato);
							if (obhntp != null) {
								OverblikHaendelseCreater.opretManuelleHaendelser(pAftale, obhntp, pDato);
							}
						}
					}
				} else {
					boolean startet = PersistensService.transactionBegin();
					TotalkundeManager.getInstance().udfoerTotalkundeKontrol(pIndivid, pDato, false);
					if (startet)
						PersistensService.transactionCommit();
				}
			}
		}
		// afflsut printjob
		if (pjps != null) {
			pjps.setOgGemAfSlutPrintjob(true);
		}
	}

	/**
	 * Hvis metoden kaldes anvendes den givne dato til at loade egenskabssystemet i stedet for BO'ets dato
	 *
	 * @param pDato
	 */
	public void setAlternativLoadDato(BigDecimal pDato) {
		alternativLoadDato_ = pDato;
	}

	public void setAendringTilGodkendelseStatus() {
		getAftaleStatustyper(); // Kun til init.

		StatusBO stsBO = getStatus()[0];
		if (stsBO.getEntitet() != null && stsBO.getEntitet().getGld().compareTo(getDato()) == 0) {
			if (stsBO.isStoptype()) { // ER stoptype
				if (stsBO.isSelecteret()) {
					if (stsBO.getBrugerTekst().indexOf(Statustype.ASSAFVAENDR_TEKST) < 0) {
						stsBO.setBrugerTekst(Statustype.ASSAFVAENDR_TEKST + " " + stsBO.getBrugerTekst().trim());
					}
					return;
				}
			} else if (stsBO.getTilStatus() != null && stsBO.getTilStatus().isStoptype()) { // Skift til Stoptype
				stsBO.setSelecteret(false);
				stsBO.getTilStatus().setSelecteret(true);
				stsBO.getTilStatus().setBrugerTekst(Statustype.ASSAFVAENDR_TEKST);
				add(stsBO.getTilStatus());
				return;
			}
		}

		Statustype stsTp = StatustypeImpl.getStatusType(Statustype.ASSAFVAENDR);
		stsBO = new StatusBO(stsTp, this, getDato());
		stsBO.setSelecteret(true);
		stsBO.setBrugerTekst(Statustype.ASSAFVAENDR_TEKST);
		add(stsBO);
	}

	/**
	 * @return the adfaerdStrategy
	 */
	public BusinessObjectAdfaerdStrategy getAdfaerdStrategy() {
		return adfaerdStrategy_;
	}

	/**
	 * @param pAdfaerdStrategy the adfaerdStrategy_ to set
	 */
	public void setAdfaerdStrategy_(BusinessObjectAdfaerdStrategy pAdfaerdStrategy) {
		this.adfaerdStrategy_ = pAdfaerdStrategy;
	}

	@Override
	public BusinessObject addNewCopyOf(BusinessObject pSource) {
		if (pSource instanceof AfregningstypeBO) {
			AfregningstypeBO bo = new AfregningstypeBO(pSource.getType(), this, ((AfregningstypeBO) pSource).getWrapper(), this.getDato(), null);
			afregningstyper.add(bo);
			return bo;
		}
		if (pSource instanceof YdelseBO) {
			YdelseBO bo = new YdelseBO(pSource.getType(), this, pSource.getDato());
			ydelser.add(bo);
			return bo;
		}
		if (pSource instanceof KlausulBO) {
			KlausulBO bo = new KlausulBO((KlausulBO) pSource);
			klausuler.add(bo);
			return bo;
		}
		if (pSource instanceof FrekvensBO) {
			FrekvensBO bo = new FrekvensBO((Frekvens) pSource.getType(), this, this.getDato());
			frekvens.add(bo);
			return bo;
		}
		if (pSource instanceof ForfaldBO) {
            ForfaldBO bo = new ForfaldBO((Forfaldstype) pSource.getType(), this, this.getDato());
            ForfaldBO srcForfald = (ForfaldBO) pSource;
            bo.setMaanede(srcForfald.getMaanednr());
			forfald.add(bo);
			return bo;
		}
		if (pSource instanceof TilgangAfgangBO) {
			TilgangAfgangBO bo = new TilgangAfgangBO(pSource.getType(), this, getDato(), null);
			tilAfgangOplysninger.add(bo);
			return bo;
		}
		if (pSource instanceof GenstandBO) {
			GenstandBO bo = (GenstandBO) pSource.clone();
			genstande.add(bo);
			return bo;
		}
		if (pSource instanceof RabatBO) {
			RabatBO bo = (RabatBO) pSource.clone();
			if (rabatterAllePerioder == null)
				rabatterAllePerioder = new ArrayList<>();
			rabatterAllePerioder.add(bo);
			return bo;
		}
		if (pSource instanceof MinPraemieBO) {
			MinPraemieBO bo = (MinPraemieBO) pSource.clone();
			if (minPraemie == null)
				minPraemie = new ArrayList<>();
			minPraemie.add(bo);
			return bo;
		}
		if (pSource instanceof AdresseBO) {
			AdresseBO bo = (AdresseBO) pSource.clone();
			adresser.add(bo);
			return bo;
		}
		return super.addNewCopyOf(pSource);
	}

	/**
	 * Anvendes til at trigge en ydelsesberegning
	 */
	public void bestilYdelsesberegning() {
		ydelsesberegningBestilt_ = true;
	}

	/**
	 * @return svaret på om der er bestilt en ydelsesberegning
	 */
	public boolean isYdelsesberegningBestilt() {
		return ydelsesberegningBestilt_;
	}

	/**
	 * Dækninger på vej til annullering skal altid have "fuld ristorno".
	 * NB -- {@link AftaleBO#saveAll()} skal være udført inden du kommer herned.
	 */
	public boolean behandlDaekningAnnullering() {
		ArrayList<DaekningBO> annullDaekninger = getAnnulleredeDaekningerListe();
		if (annullDaekninger != null && annullDaekninger.size() > 0) {
			for (int k = 0; k < annullDaekninger.size(); k++) {
				AftaleEgenbetaltOmkost ebomk = PersistensService.opret(AftaleEgenbetaltOmkostImpl.class);
				ebomk.setAftale(getAftale());
				ebomk.setProdukt(((Produkt) (annullDaekninger.get(k)).getEntitet()));
				ebomk.setAfgaeldendefra(getAftale().getGld());
				ebomk.setAfophoersdato(getAftale().getOph());
				ebomk.setEgenbetaltomkostjn(false);
				ebomk.setAftaleannulleretj_n(false);
				ebomk.setAnnulleretjn(true);
				ebomk.setAftaleophoertj_n(false);
				ebomk.setOphoertjn(false);
				ebomk.setUdfoertjn(false);
				ebomk.setPdgaeldendefra((annullDaekninger.get(k)).getEntitet().getGld());
				ebomk.setPdophoersdato((annullDaekninger.get(k)).getEntitet().getOph());
				PersistensService.save(ebomk);
			}
			daekningerRegAnnul = null;
			return true;
		}
		return false;
	}

	/**
	 * OBSF5D2 Tidligere ophør
	 *
	 * @return
	 */
	public boolean isMulighedForTidligereOphoer() {
		List<DaekningBO> alleDaekninger = getAlleDaekninger();
		if (alleDaekninger != null) {
			for (DaekningBO dkBO : alleDaekninger) {
				if (!dkBO.isNew() &&
						((Produkt) dkBO.getEntitet()).findesIReasProduktOphoer() &&
						dkBO.isMulighedForTidligereOphoer()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * OBSF5D2 Tidligere ophør
	 */
	public void setStatusTyperTilTidligereOphoer() {
		AftaleStatustype afSttp = aftale.getAftaleStatustypeNyesteUdenOphoer();
		afSttp.setOph(afSttp.getGld());
		PersistensService.gem(afSttp);

		AftaleStatustype[] afSttpTab = aftale.getAftaleStatustype(StatustypeImpl.getStatusType(Statustype.IKRAFT), true);
		if (afSttpTab == null) {
			// SHS : hvad skal der så ske - må ikke køre videre...
//			Enten
//			- send en mail til hotline at der mangler en Ikraft statustype på aftalen (hvis ikke det sker for tit er det en beskeden opgave at tilføje en)
//			eller
//			- return (uden at gøre noget som helst -- fejlen er jo ikke specielt kritisk og ophøret kan ikke fjernes på dette tidspunkt).
			return;
		}
		Arrays.sort(afSttpTab, new ModelObjektGaeldendeComparator());
		afSttp = PersistensService.opret(AftaleStatustypeImpl.class);
		afSttp.setAftale(aftale);
		afSttp.setStatustype(afSttpTab[afSttpTab.length - 1].getStatustype());
		afSttp.setGld(afSttpTab[afSttpTab.length - 1].getGld().compareTo(getDato()) > 0 ? getDato() : afSttpTab[afSttpTab.length - 1].getGld());
		afSttp.setStatustekst("Tidligere ophør.");
		PersistensService.gem(afSttp);
		aftale.addAftaleStatustype(afSttp);
	}

	public void validate() throws GensamValidateException {
		if (this.getParent() instanceof PanthaverBO && this.ediPanthaverdeklarationDb_ != null) {

			verifyICDDaekning();

			/**
			 * Hvis E-deklaration skal vi sikre, at vi ved nye får prompted for en accept-kode.
			 */
			EgenskabSammenhaenge f = getAftaleEgenskabSystem().getEgenskabSammenhaeng(EdiPanthaverService.getInstance().getDeklarationstypeGruppe());
			if (f != null && f.getIndtastetVaerdi() != null) {
				Deklarationstype editype = Panthaverdeklarationstype.getPanthaverdeklarationstype(f.getIndtastetVaerdi());
				if (editype != null && editype.isZPE_AcceptRelevant() && ediDeklarationstype_E_acceptkoder == null) {
					GensamValidateException ex = new GensamValidateException();
					ex.setFejltypenr("CAPTION2");
					ex.setFejlnr("GENEREL16");
					ex.addFejltekstParameter("*prompt");

					throw ex;
				}
			}
		}
	}

	private void verifyICDDaekning() throws GensamValidateException {
		EDIPanthaverDokument ediphdok = ediPanthaverdeklarationDb_.getAsDocument();
		GenstandBO genstandbo = (GenstandBO) getInstanceOf(GenstandBO.class);
		boolean match = genstandbo.matchDaekningstyperICDDaekningstype(ediphdok.getICD(), true);
		if (!match) {
			GensamValidateException ex = new GensamValidateException(this);
			ex.setFejltypenr("CAPTION2");
			ex.setFejlnr("PANTHAVER_PAC_6");
			ex.addFejltekstParameter(ediphdok.getICD().getForsikringstypeDetail_ICD_C331_4495());
			ex.setMessageType(JOptionPane.ERROR_MESSAGE);
			throw ex;

		}
	}


	protected final void addToValidatePrType(BusinessObject parent) {
		if (boerMarkedToValidatePrType == null)
			boerMarkedToValidatePrType = new HashSet<>();
		boerMarkedToValidatePrType.add(parent);

	}

	@Override
	protected void validatePrRegelsaettype() throws GensamValidateException {
		/**
		 * Hvis der undervejs i valideringen er opsamlet bo'er, skal disse bo'er valideres for at de type for type har præcis
		 * samme status i betydningen selected og evt. numerisk angivelse.<br>
		 *     Lige nu brugt til at sikre, at Him og Con's ULY-børn har samme relevante dækninger og summer.
		 * </br>
		 */
		if (boerMarkedToValidatePrType != null && !boerMarkedToValidatePrType.isEmpty()) {
			Map<RegelsaetType, Object> syncMap = new HashMap<>();
			Iterator<BusinessObject> iterator = boerMarkedToValidatePrType.iterator();
			while (iterator.hasNext()) {
				BusinessObject child = iterator.next();
				RegelsaetType type = child.getType();
				Object vaerdi = syncMap.get(type);
				if (vaerdi != null) {
					if (!vaerdi.equals(child.getValueToCompare())) {
						GensamValidateException ex = new GensamValidateException(this);
						ex.setFejltypenr("CAPTION10");
						ex.setFejlnr("SAMMENHAENG_XTJEK");
						ex.addFejltekstParameter(type.getBenaevnelse().trim());
//						ex.addFejltekstParameter(vaerdi.toString());                      Viser lige nu ikke noget meningsfuldt
//						ex.addFejltekstParameter(child.getValueToCompare().toString());   Viser lige nu ikke noget meningsfuldt
						ex.setMessageType(JOptionPane.ERROR_MESSAGE);
						throw ex;
					}
				}
				syncMap.put(type, child.getValueToCompare());
			}
		}
	}

	/**
	 * @param pEdiDeklarationstype_E_acceptkoder the ediDeklarationstype_E_acceptkoder to set
	 */
	public void setEdiDeklarationstype_E_acceptkoder(List<String> pEdiDeklarationstype_E_acceptkoder) {
		this.ediDeklarationstype_E_acceptkoder = ContainerUtil.toArray(pEdiDeklarationstype_E_acceptkoder);
	}

	/**
	 * Hvis initieret skal save af til- og afgangsoplysninger erstattes af en overtagelse / flytning og ikke kopiering
	 */
	private Aftale overtagTilAfgangsoplysningerFra_ = null;

	/**
	 * Skal save af til- og afgangsoplysninger erstattes af en overtagelse / flytning og ikke kopiering?<br>
	 * Må kun gøres meningsfyldt hvis det andet bo bliver annulleret.
	 *
	 * @param pAftaleFra
	 */
	public void setOvertagTilAfgangsoplysningerFra(Aftale pAftaleFra) {
		overtagTilAfgangsoplysningerFra_ = pAftaleFra;
	}

	public boolean hasOvertagTilAfgangsoplysningerFra() {
		return overtagTilAfgangsoplysningerFra_ != null;
	}

	public String getFrekvensValgt() {
		for (FrekvensBO frekvens : getFrekvens()) {
			if (frekvens.isSelecteret()) return frekvens.toString();
		}
		return "";
	}

	public FrekvensBO getFrekvensBOValgt() {
		for (FrekvensBO frekvens : getFrekvens()) {
			if (frekvens.isSelecteret())
				return frekvens;
		}
		return null;
	}

	public Frekvens getFrekvensObjektValgt() {
		for (FrekvensBO frekvens : getFrekvens()) {
			if (frekvens.isSelecteret())
				return (Frekvens) frekvens.getType();
		}
		return null;
	}

	/**
	 * Finder en værdi iht. en givet kortbenævnelse.
	 *
	 * @param pKortBenaevnelse
	 * @return Null hvis en værdi ikke kunne findes.
	 */
	public String getEgenskabVaerdi(String pKortBenaevnelse) {
		AftaleEgenskabSystem egenskabSystem = getAftaleEgenskabSystem();
		if (egenskabSystem == null) {
			return null;
		}
		OrderedCollection egenskabAttributter = egenskabSystem.getEgenskabAttributter();
		EgenskabAttribut egenskabAttribut = getEgenskabAttribut(egenskabAttributter, pKortBenaevnelse);
		if (egenskabAttribut == null) {
			return null;
		}
		return egenskabAttribut.getVaerdiPresentation();
	}

	/**
	 * Udsøger en EgenskabAttribut i en OrderedCollection hvor egenskabsgruppen har en bestemt kortbenævnelse.
	 *
	 * @param pCollection      Collectionen der skal udsøges i.
	 * @param pKortBenaevnelse En kortbenævnelse til egenskabsgruppen.
	 * @return En EgenskabAttribut hvis der blev fundet en der passer på kortbenævnelsen ellers null.
	 */
	private EgenskabAttribut getEgenskabAttribut(OrderedCollection pCollection, String pKortBenaevnelse) {
		for (int i = 0; pCollection != null && i < pCollection.size(); i++) {
			if (((EgenskabAttribut) pCollection.get(i)).getEgenskabsgruppe().getKortBenaevnelse().trim().equals(pKortBenaevnelse.trim()))
				return (EgenskabAttribut) pCollection.get(i);
		}
		return null;
	}

	/**
	 * @return forsikringens minimumspræmie eller null hvis intet minimum
	 */
	public BigDecimal getMinimumPraemie() {
		// TODO Denne metode skal verificeres mht. virkemåde. Accessvejen er lidt pudsig og hvad med aftminppt?
		// Når fastlagt bør der refactes ud i mindre metoder - og nok i bedre hjem
		List<MinPraemieBO> minPraemier = getMinPraemie();
		if (minPraemier != null && !minPraemier.isEmpty()) {
			MinPraemieBO minPraemieBO = minPraemier.get(0); // max een instans
			BigDecimal brugerbestemtMin = minPraemieBO.getBeloebBruger();
			if (brugerbestemtMin != null && brugerbestemtMin.doubleValue() > 0.00) {
				return brugerbestemtMin;
				// Den brugerbestemte minimumspræmie overskriver alle regler
			}
		}
        List<GenstandBO> genstandeSelekteret = getGenstandeSelekteret();
		if (genstandeSelekteret == null || genstandeSelekteret.isEmpty())
		    return null;

		// Ingen brugerbestemt - findes der overhovedet regler?
		// Uklart om nedenstående typeaflæsning kan være sand hvis intet bo
		Aftaletype aftaletype = (Aftaletype) this.getType();
		AftpMinPrae[] aftpMinPrae = aftaletype.getAftpMinPrae(getRegelaflaesningsdato(getDato()));
		if (aftpMinPrae != null && aftpMinPrae.length > 0) {
            return udsoegRegelbestemtMinimum(genstandeSelekteret, aftpMinPrae);
		}
		return null;
	}

    private BigDecimal udsoegRegelbestemtMinimum(List<GenstandBO> genstandeSelekteret, AftpMinPrae[] aftpMinPrae) {
        BigDecimal defaultMinimum = null;
        BigDecimal currentBedsteBud = BigDecimal.ZERO;

        for (GenstandBO genstand : genstandeSelekteret) {
            boolean harHaftMatchOplysning = false;
            for (AftpMinPrae aftpMinPrae2 : aftpMinPrae) {
                AftaletypeMinPraeOplysning[] aftaletypeMinPraeOplysning = aftpMinPrae2.getAftaletypeMinPraeOplysning();
                if (aftaletypeMinPraeOplysning != null && aftaletypeMinPraeOplysning.length > 0) {
                    // Så skal vi finde den med en Oplysning der er opfyldt på  this
                    for (AftaletypeMinPraeOplysning aftaletypeMinPraeOplysning2 : aftaletypeMinPraeOplysning) {
                        if (!aftaletypeMinPraeOplysning2.isAnnulleret() && aftaletypeMinPraeOplysning2.isGld(getDato())) {
                            Oplysning oplysning = aftaletypeMinPraeOplysning2.getOplysning();
                            BusinessObject startpunkt = genstand;

                            OplysningsAftjekWrapper value = startpunkt.searchBottomUp(new OplysningsAftjekWrapper(oplysning, getRegelaflaesningsdato(getDato()),
                                    getDato(), false));
                            if (value.hasValue() && value.equalsEgenskab(new StringBuilder())) {
                                harHaftMatchOplysning = true;
                                if (aftpMinPrae2.getBeloeb().compareTo(currentBedsteBud) > 0)
                                    currentBedsteBud = aftpMinPrae2.getBeloeb();
//                                return aftpMinPrae2.getBeloeb();
                            }
                        }
                    }
                } else {
                    defaultMinimum = aftpMinPrae2.getBeloeb();
                    // Hvis der er et mix af relationer til oplysning og ingen oplysning er sidstnævnte default
                }
            }
            if (!harHaftMatchOplysning) {
                if (defaultMinimum != null && defaultMinimum.compareTo(currentBedsteBud) > 0)
                    currentBedsteBud = defaultMinimum;
            }
        }
        if (currentBedsteBud.compareTo(BigDecimal.ZERO) > 0)
            return currentBedsteBud;
        return defaultMinimum;
    }

    /**
	 * Giver kun mening hvis PrisberegningsService har udført en præmieberegning
	 *
	 * @return Den beregnede og fordelte minimumspræmie
	 */
	public BigDecimal getMinimumPraemieBeregnet() {
		BigDecimal rtnMinimumspraemie = GaiaConst.NULBD2dec;
		List<DaekningBO> daekningBOList = getDaekningerSelekteredeDaekninger();
		for (DaekningBO daekningBO : daekningBOList) {
			if (daekningBO.getPraemieBeregner() != null) {
				BigDecimal praemieJustering = daekningBO.getPraemieBeregner().getPraemieJustering();
				if (praemieJustering != null && praemieJustering.compareTo(BigDecimal.ZERO) > 0) {
					rtnMinimumspraemie = rtnMinimumspraemie.add(praemieJustering);
				}
			}
		}
		return rtnMinimumspraemie;
	}

	/**
	 * Ansvarlig for save af ændring i BS-status og -oplysninger incl. afregningstyper
	 */
	public void handleBSTilmelding() {
		PBSTilmeldingBO pbs = getPBSTilmelding();
		boolean save = false;
		if (pbs.isMedfoererTilmelding()) {
			setAfregningstypePBS();
			AfregningManager.getInstance().changeAfregningstypeToPbs(getAftale());
			save = true;
		} else if (pbs.isMedfoererAfmelding()) {
			setAfregningstypeGiro();
			AfregningManager.getInstance().changeAfregningstypeFromBetalingsServiceToGiro(getAftale());
			save = true;
		}
		if (save || (pbs.isModified() && pbs.isEditable())) {
            skipAfledteopdateringer1();
			saveAll();
		}
	}

    /**
     * Skal kaldes mellem transactionBegin og saveAll hvis det skal have nogen effekt og kan ikke omgøres senere for samme instans.
     * Hvis den kaldes skippes en række afledte opdateringer.
     * Klienten skulle nok slet ikke kalde saveAll på this....
     *
     * OBS denne udgave disabler revideringshåndtering for alle aftaleboer i samme transaction
     */
    public void skipAfledteopdateringer1() {
        PersistensService.getRevideringsBufferCurrent().setUndladRevidering(true);
        skipAfledteopdateringer2();
    }

    /**
     * Skal kaldes før saveAll hvis det skal have nogen effekt og kan ikke omgøres senere for samme instans af this.
     * Hvis den kaldes skippes en række afledte opdateringer.
     *
     * OBS denne udgave disabler kun dele af revideringshåndtering og kun for this - ikke andre aftaleboer i samme transaction
     */
    public void skipAfledteopdateringer2() {
        setUndladUdvalgteRevideringer(true);
        setYdelsesBeregningEnabled(false);
        setAftalehaendelsestypeAlternativ(null); // opretter slet ikke aftalehændelse
        setUndladTotalkundeKontrol(true);
        setUndladOmraadeKonsekvensVedSaveAll(true);
    }

    /**
	 * Returnerer hovedforfaldsmåneden på et upersisteret aftaleBO
	 **/
	public String getHovedforfaldsmaaned() {
		List<ForfaldBO> forfald = this.getForfald();
		if (forfald != null) {
			for (ForfaldBO forfaldBO : forfald) {
				if (forfaldBO.isSelecteret()) {
					return Datobehandling.getMaanedBenaevnelse(forfaldBO.getMaanednr());
				}
			}
		}
		return "";
	}

	public int getHovedforfaldsmaanednummer() {
		List<ForfaldBO> forfald = this.getForfald();
		if (forfald != null) {
			for (ForfaldBO forfaldBO : forfald) {
				if (forfaldBO.isSelecteret()) {
					return forfaldBO.getMaanednr().intValue();
				}
			}
		}
		return -1;
	}


	public void setForsikringWrapper(ForsikringWrapper pForsikringWrapper) {
		this.forsikringWrapper = pForsikringWrapper;
	}

	public void setForsikringArt(Forsikringstype pForsikringsart) {
		this.forsikringsart = pForsikringsart;
	}

	public Forsikringstype getForsikringsArt() {
		return this.forsikringsart;
	}

	public Forsikringstype getForsikringsType() {
		return Aftaletype.Forsikringstype.getForsikringstype(getType().getKortBenaevnelse());
	}

	public ForsikringWrapper getForsikringWrapper() {
		return this.forsikringWrapper;
	}

	public boolean hasEgenskabsystemLoaded() {
		return aftaleEgenskabSystem != null;
	}

	/**
	 * Metoden returnerer de individer, der er registreret som forsikringstager og evt. medejer(e) / medforsikrede på
	 * forsikringen.
	 *
	 * @param pInclMedforsikrede true hvis metoden skal returnere medforsikrede på genstandsniveau
	 * @return List<Individ> hvor den første altid er forsikringstager og de næste individer med
	 * forholdsbeskrivelsen Medejer eller (optional) Medforsikret
	 */
	public List<Individ> getEjere(boolean pInclMedforsikrede) {
		List<Individ> ejere = new ArrayList<>(2);
		List<GenstandBO> gns = getGenstandeSelekteret();
		if (true) {
			ejere.add((Individ) getParent().getEntitet());

			List<RelationsBO> relationerAftale = getRelationer();
			for (RelationsBO rbo : relationerAftale) {
				if (!rbo.isSelecteret())
					continue;
				IntpAftpFhbsk smh = (IntpAftpFhbsk) rbo.getForholdsbeskrivelse();
				if (smh != null && smh.isMedejer()) {
					Individ r = (Individ) rbo.getEntitet();
					if (!ejere.contains(r))
						ejere.add(r);
				}
			}
			if (gns != null) {
				for (GenstandBO genstand : gns) {
					List<RelationsBO> rels = genstand.getRelationer();
					if (rels != null) {
						for (RelationsBO rel : rels) {
							if (rel != null && rel.getForholdsbeskrivelse() != null &&
									rel.isSelecteret() &&
									((IntpGntpsmhbsk) rel.getForholdsbeskrivelse()).isMedejer()) {
								Individ r = (Individ) rel.getEntitet();
								if (!ejere.contains(r))
									ejere.add(r);
							}
						}
					}
				}
			}

		}
		if (pInclMedforsikrede) {
			// ULY? En evt. medforsikret er at betragte som ejer.
			if (gns != null) {
				for (GenstandBO genstand : gns) {
					List<RelationsBO> rels = genstand.getRelationer();
					if (rels != null) {
						for (RelationsBO rel : rels) {
							if (rel != null && rel.getForholdsbeskrivelse() != null &&
									rel.isSelecteret() &&
									((IntpGntpsmhbsk) rel.getForholdsbeskrivelse()).isForsikretVoksen()) {
								Individ r = (Individ) rel.getEntitet();
								if (!ejere.contains(r))
									ejere.add(r);
							}
						}
					}
				}
			}
		}
		return ejere;
	}

	/**
	 * @return liste med forsikrede voksne og børn
	 */
	public List<Individ> getForsikredeVoksneBoern() {
		List<Individ> forsikrede = new ArrayList<>();
		List<GenstandBO> gns = getGenstandeSelekteret();
		if (gns != null) {
			for (GenstandBO genstand : gns) {
				List<RelationsBO> rels = genstand.getRelationer();
				if (rels != null) {
					for (RelationsBO rel : rels) {
						if (rel != null && rel.getForholdsbeskrivelse() != null &&
								rel.isSelecteret() &&
								rel.getForholdsbeskrivelse().isForsikret()) {
							Individ r = (Individ) rel.getEntitet();
							if (!forsikrede.contains(r))
								forsikrede.add(r);
						}
					}
				}
			}
		}
		return forsikrede;
	}

	private BigDecimal forlaengFleraarigDato = null;
	private boolean forlaengFleraarigDatoIsSet = false;
	private boolean udlaegForlaengFleraarig = false;
	private BigDecimal udlaegForlaengBesoegsdato = null;
	private Aftalehaendelse alleredeUdlagtForlaengelse = null;
	private Aftalehaendelse udlagtForlaengelse = null;

	public BigDecimal getForlaengFleraarigDato() {
		if (forlaengFleraarigDatoIsSet) {
			return forlaengFleraarigDato;
		}
		if (dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.HAR_FORLAENG_FLERAARIG.isPresent()) {
			// Faktisk aftale
			// Fornyet mindst een gang før ændringsdato
			// Har flerårig tegning -- der jo så kan forlænges
			if (getEntitet() != null) {
				if (((Aftale) getEntitet()).getSenesteUdfoertFornyelsesdato(getDato()) != null) {
					if (((Aftale) getEntitet()).hasFleraarigTegning(getDato())) {
						forlaengFleraarigDatoIsSet = true;
						// Vi skal have fat i seneste fornyelse på eller efter ændringsdatoen
						forlaengFleraarigDato = ((Aftale) getEntitet()).getSenesteUdfoertFornyelsesdato();
						if (forlaengFleraarigDato == null || forlaengFleraarigDato.compareTo(getDato()) < 0) {
							forlaengFleraarigDato = ((Aftale) getEntitet()).getIkkeUdfoertFornyelsesopgave();
						}
						if (forlaengFleraarigDato != null) {
							alleredeUdlagtForlaengelse = getAlleredeUdlagtForlaengelse();
						}
						return forlaengFleraarigDato;
					}
				}
			}
		}
		return null;
	}

	public boolean harAlleredeUdlagtForlaengelse() {
		return alleredeUdlagtForlaengelse != null;
	}


	public void setUdlaegForlaengFleraarig(BigDecimal pDato) {
		udlaegForlaengFleraarig = true;
		udlaegForlaengBesoegsdato = pDato;
	}

	public Aftalehaendelse getUdlagtForlaengelse() {
		return udlagtForlaengelse;
	}

	private void udlaegForlaengHaendelse() {
		if (harAlleredeUdlagtForlaengelse()) {
			return;
		}

		AftalehaendelseImpl aftalehaendelse = PersistensService.opret(AftalehaendelseImpl.class);
		aftalehaendelse.setHaendelsestype(Aftalehaendelse.HAENDELSESTYPE_FORLAENG_FLERAARIG_TEGNING);
		aftalehaendelse.setAftale((Aftale) getEntitet());
		aftalehaendelse.setHaendelsesdato(forlaengFleraarigDato);
		aftalehaendelse.setBesoegsdato(udlaegForlaengBesoegsdato);
		PersistensService.save(aftalehaendelse);
		udlagtForlaengelse = aftalehaendelse;
	}

	private Aftalehaendelse getAlleredeUdlagtForlaengelse() {
		Aftalehaendelse[] aftalehaendelseTab = ((Aftale) getEntitet()).getAftalehaendelse(forlaengFleraarigDato);
		if (aftalehaendelseTab != null) {
			for (Aftalehaendelse aftalehaendelse : aftalehaendelseTab) {
				if (aftalehaendelse.getHaendelsestype().equals(Aftalehaendelse.HAENDELSESTYPE_FORLAENG_FLERAARIG_TEGNING)) {
					return aftalehaendelse;
				}
			}
		}
		return null;
	}

	/**
	 * Udfør en ny omkostningsberegning af aftalen
	 */
	public void beregnOmkostning() {
		// HOT-22055 Regelaflæsningsdato for Omkostningsberegning er beregningsdato
//		BigDecimal regelaflaesningsdato = getRegelaflaesningsdato(getDato());
		BigDecimal regelaflaesningsdato = getDato();
		if (beregningsDebugger_ == null)
			beregningsDebugger_ = new BeregningsDebugger();
		if (this.omkostBeregner == null) {
			this.omkostBeregner = new OmkostningBeregner(this, getDato(), regelaflaesningsdato, beregningsDebugger_);
		}
		omkostBeregner.setBeregningsdato(getDato());
		omkostBeregner.setRegelaflaesningsdato(regelaflaesningsdato);
		omkostBeregner.beregn();
	}

	/**
	 * @return den omkostningsberegner der tilknyttet dækningsboet
	 */
	public OmkostningBeregner getOmkostningBeregner() {
		return this.omkostBeregner;
	}

	/**
	 * Skal sættes udefra for nye aftaleobjekter hvis den afviger fra boets getDato<br>
	 * Har ingen effekt for persisterede objekter
	 *
	 * @param pDato
	 */
	public void setRegelaflaesningsdato(BigDecimal pDato) {
		regelaflaesningsdatoUdefra_ = pDato;
		resetTarifDatocached();
	}

    /**
     * skal kaldes hvis der er nogen som helst risiko for at datoen har ændret sig siden caching af datoen
     */
	public void resetTarifDatocached() {
	    tarifDatocached_ = null;
    }
    /**
     *
     * @return Tarifdato subsidiært regelaflæsningsdato
     */
	public final BigDecimal getTarifdatoSubsidiaertRegelaflaesningsdato() {
	    if (tarifDatocached_ == null) {
            tarifDatocached_ = getTarifDatoFraFelt();
            if (tarifDatocached_ == null)
                tarifDatocached_ = getRegelaflaesningsdato(getDato());
        }
        return tarifDatocached_;

    }

	/**
	 * @param pBeregningsdato
	 * @return aftalens angivne eller beregnede regelaflæsningsdato i forhold til pBeregningsdato
	 */
	public final BigDecimal getRegelaflaesningsdato(BigDecimal pBeregningsdato) {
		if (!isNew()) {
			if (regelaflaesningsdatoBeregnet_ == null) {
//				regelaflaesningsdatoBeregnet_ = aftale.getSenesteUdfoertFornyelsesdato();
				regelaflaesningsdatoBeregnet_ = aftale.findSenesteHovedForfaldsDato(pBeregningsdato);
//				if (regelaflaesningsdatoBeregnet_ == null)
//					regelaflaesningsdatoBeregnet_ = aftale.getGld();
				if (aftale.isTilbud()) {
					// asgm 26680 hvis kopieret fra forsikring, vinder forsikringens dato
					Aftale forsikringOprindeligeFraThisTilbud = aftale.getForsikringOprindeligeFraThisTilbud();
					if (forsikringOprindeligeFraThisTilbud != null) {
						regelaflaesningsdatoBeregnet_ = forsikringOprindeligeFraThisTilbud.findSenesteHovedForfaldsDato(pBeregningsdato);
//						if (regelaflaesningsdatoBeregnet_ == null)
//							regelaflaesningsdatoBeregnet_ = forsikringOprindeligeFraThisTilbud.getGld();

					}
				}
			}
			return regelaflaesningsdatoBeregnet_;
		} else {
			if (regelaflaesningsdatoUdefra_ == null) {
				regelaflaesningsdatoUdefra_ = getDato();
			}
			return regelaflaesningsdatoUdefra_;
		}
	}

    /**
     *
     * @return alternativ regelaflæsningsdato fra aftaleegenskabsgruppe TARIFDATO, null hvis den ikke findes
     */
    public final BigDecimal getTarifDatoFraFelt() {
        EgenskabSystem.EgenskabSammenhaenge tarifdato = getEgenskabSystem().getEgenskabSammenhaeng(Aftaleegngrp.Aftalefelt.TARIFDATO.getKortBenaevnelse());
        if (tarifdato != null &&
                tarifdato.getIndtastetVaerdi() != null && !tarifdato.getIndtastetVaerdi().trim().isEmpty()){
            try {
                BigDecimal vendtDato = Datobehandling.skaermDatoTilBigDecimal(tarifdato.getIndtastetVaerdiAsDBString(), "ddMMyyyy");
                return vendtDato;
            } catch (ParseException e) {
                e.printStackTrace();
                throw new DatafejlException("Der skal være angivet en valid tarifDato");
            }
        }
        return null;
    }


    /**
	 * En simuleret rabatpct. der kan påtvinges rabatberegneren<br>
	 * Se {@link dk.gensam.gaia.business.beregning.RabatBeregner#setUdeFraSatFakePctDerSkalMedIBeregningen(double)}
	 */
	private double simuleretRabatPct = 0.0;

	/**
	 * @see #simuleretRabatPct
	 */
	public double getSimuleretRabatPct() {
		return simuleretRabatPct;
	}

	/**
	 * @see #simuleretRabatPct
	 */
	public void setSimuleretRabatPct(double simuleretRabatPct) {
		this.simuleretRabatPct = simuleretRabatPct;
	}

	/**
	 * @param pStatustype
	 * @see #statustypeToUseUansetHvad
	 */
	public void setstatustypeToUseUansetHvad(Statustype pStatustype) {
		statustypeToUseUansetHvad = pStatustype;
	}

	/**
	 * @return svaret
	 * @see #isLoadedMedGenstande
	 */
	public boolean isLoadedMedGenstande() {
		return isLoadedMedGenstande;
	}

	public boolean isTilOgAfgangeMedtagFremtidige() {
		return tilOgAfgangeMedtagFremtidige;
	}

	public void setTilOgAfgangeMedtagFremtidige(boolean pBoolean) {
		tilOgAfgangeMedtagFremtidige = pBoolean;
	}

	/**
	 * @param pAlderNonPersistent
	 * @see #alderNonPersistent
	 */
	public void setAlderToSave(BigDecimal pAlderNonPersistent) {
		alderNonPersistent = pAlderNonPersistent;
	}

	/**
	 * @see #alderNonPersistent
	 */
	public BigDecimal getAlderToSave() {
		return alderNonPersistent;
	}

	public boolean isFrekvensSkift() {
		return isFrekvensSkift;
	}

	public void setIsFrekvensSkift(FrekvensBO pFrekvensBO) {
		isFrekvensSkift = true;
		frekvensSkiftBO = pFrekvensBO;
	}

	public boolean isHForfaldSkift() {
		return isHForfaldSkift;
	}

	public void setIsHForfaldSkift(ForfaldBO pForfaldBO) {
		isHForfaldSkift = true;
		hfForfaldSkiftBO = pForfaldBO;
	}

	public void overblikBemaerkningEdiopsigelseFlytTilNyAftale(Aftale pAftaleGammel) {

		OverblikHaendelseSupertype overblikHaendelsestypeEdiOpsigelse = OverblikHaendelsestypeImpl.getOverblikHaendelsestypeRegelAktiv(OverblikObjEDIOpsigelse.EDIOPSIGELSE, dato_);

		if (overblikHaendelsestypeEdiOpsigelse == null) return;

		OverblikHaendelseBemaerkning[] overblikHaendelseBemaerkninger = this.getIndivid().individ.getOverblikBemaerkninger(overblikHaendelsestypeEdiOpsigelse);
		if (overblikHaendelseBemaerkninger == null) return;
		for (OverblikHaendelseBemaerkning overblikHaendelseBemaerkning : overblikHaendelseBemaerkninger) {
			Aftale aftale = overblikHaendelseBemaerkning.getEdiOpsigelsesAftale();
			if (aftale != null) {
				if (aftale.equals(pAftaleGammel)) {
					boolean startet = PersistensService.transactionBegin();
					overblikHaendelseBemaerkning.setEdiOpsigelsesAftale(this.getAftale());
					PersistensService.gem(overblikHaendelseBemaerkning);
					if (startet) {
						PersistensService.transactionCommit();
					}
				}
			}
		}
	}

	/**
	 *
	 * @param ydelsesBeregningEnabled
	 */
	public void setYdelsesBeregningEnabled(boolean ydelsesBeregningEnabled) {
		this.ydelsesBeregningEnabled = ydelsesBeregningEnabled;
	}

	public final void setDaekningsafhaengighederEnabled(boolean p) {
		isDaekningstypeafhaengighederEnabled_ = p;
	}

	/**
	 * Håndtag til at omgå defaultadfærd, så evt. fremtidige aftaleadresser kopieres med til ny aftale
	 *
	 * @param b
	 */
	public void setSaveFremtidigeAdresser(boolean b) {
		saveFremtidigeAdresser = b;

	}

	public void fremtidsWarningForfaldFrekvens(boolean pKunFrekvens) throws GensamValidateException {
		AftaleFftpMd fremtidsAftaleFftpMd = null;
		AftaleFrekvens fremtidsAftaleFrekvens = null;
		GensamValidateException gensamValidateException = new GensamValidateException();

		if (!pKunFrekvens) {
			AftaleFftpMd[] aftaleFftpMdTab = aftale.getAftaleFftpMdMedFremtidige(getDato());
			for (AftaleFftpMd aftaleFftpMd : aftaleFftpMdTab) {
				if (aftaleFftpMd != null && aftaleFftpMd.getGld().compareTo(getDato()) > 0) {
					fremtidsAftaleFftpMd = aftaleFftpMd;
					gensamValidateException.addFejltekstParameter(Datobehandling.format(fremtidsAftaleFftpMd.getGld()));
					gensamValidateException.addFejltekstParameter(Datobehandling.getMaanedBenaevnelse(fremtidsAftaleFftpMd.getMaanedsnummeriaaret()));
				}
			}
		}

		AftaleFrekvens[] aftaleFrekvensTab = aftale.getAftaleFrekvensMedFremtidige(getDato());
		for (AftaleFrekvens aftaleFrekvens : aftaleFrekvensTab) {
			if (aftaleFrekvens != null && aftaleFrekvens.getGld().compareTo(getDato()) > 0) {
				fremtidsAftaleFrekvens = aftaleFrekvens;
				gensamValidateException.addFejltekstParameter(Datobehandling.format(fremtidsAftaleFrekvens.getGld()));
				gensamValidateException.addFejltekstParameter(fremtidsAftaleFrekvens.getFrekvens().getBenaevnelse().trim());
			}
		}

		if (fremtidsAftaleFftpMd != null) {
			if (fremtidsAftaleFrekvens != null) {
				gensamValidateException.setFejlnr("ADVARSEL_SKIFT_FORFALD_FREKVENS_FREMTID");
			}
			else {
				gensamValidateException.setFejlnr("ADVARSEL_SKIFT_FORFALD_FREMTID");
			}
		}
		else {
			if (fremtidsAftaleFrekvens != null) {
				gensamValidateException.setFejlnr("ADVARSEL_SKIFT_FREKVENS_FREMTID");
			}
			else {
				return;
			}
		}
		gensamValidateException.setFejltypenr("CAPTION4");
		gensamValidateException.setMessageType(JOptionPane.QUESTION_MESSAGE);
		throw gensamValidateException;
	}

	/**
	 *
	 * @return svaret på om der må og skal anvendes nyt regelsæt
	 */
	public final boolean isMedNyeRegler() {
		if (medNyeRegler_ ||
				this.isNew() ||
				this.isTilbud())
			return true;
			// todo verificere isTilbud
			// todo verificere ændring pr. ikraft på forsikring
		return false;
	}

	/**
	 *
	 * @return svaret på om der må og skal anvendes nyt regelsæt
	 */
	public final boolean isUdenNyeReglerMenMedDaekningsload() {
		return udenNyeReglerMenMedDaekningsload_;
	}

	/**
	 * Hvis du virkelig mener, at DU må basere dig på nyt regelsæt, skal du kalde denne metode.
	 * Unødvendigt hvis this isNew.
	 *
	 * @param pMedNyeRegler
	 */
	public void setMedNyeRegler(boolean pMedNyeRegler) {
		this.medNyeRegler_ = pMedNyeRegler;
	}

	/**
	 * Hvis du virkelig mener, at DU må loade ikke-valgte dækninger, skal du kalde denne metode.
	 * Unødvendigt hvis this isNew eller du også har kaldt setMedNyeRegler(true)
	 *
	 * @param udenNyeReglerMenMedDaekningsload
	 */
	public void setUdenNyeReglerMenMedDaekningsload(boolean udenNyeReglerMenMedDaekningsload) {
		this.udenNyeReglerMenMedDaekningsload_ = udenNyeReglerMenMedDaekningsload;
	}


    protected boolean isSkipLoadIndividetsAdresser() {
        return skipLoadIndividetsAdresser;
    }

    public void setSkipLoadIndividetsAdresser(boolean skipLoadIndividetsAdresser) {
        this.skipLoadIndividetsAdresser = skipLoadIndividetsAdresser;
    }

    /**
     * TODO Redesign 04-01-2018
     * 	Dette er virkeligt uheldigt placeret. Helt principielt kan en aftale ikke have en alder i denne forstand.
	 *	Brug kun i yderste nødstilfælde eller redesign det. /04.10.2018-pkr
	 *
	 *  Understøtter at forsikringstagers alder er sat på aftalebo og individbo, og understøtter fødselsdato
     *
     * @param individ
     * @param dato  opgørelsesdato
     * @return alder, null hvis ikke muligt at beregne
     */
    public BigDecimal getIndividAlder(IndividBO individ, BigDecimal dato) {
        BigDecimal alder = getAlderToSave();
        if (alder == null) {
            if (individ.isNew()) {
                EgenskabAttribut attribut = individ.getEgenskabSystem().getEgenskabAttribut(Individegenskabsgrp.CPRNR.trim());
                if (attribut != null) {
                    BigDecimal foedselsdato = IndividImpl.getFoedselsdato(attribut.getVaerdiPresentation(), false, false);
                    if (foedselsdato != null) {
                        alder = Datobehandling.antalHeleAar(foedselsdato, dato);
                    }
                }
            } else {
                if (individ.getAlderToSave() != null)
                    return individ.getAlderToSave();
                alder = ((Individ) individ.getEntitet()).afgoerAlder(dato);
            }
        }

        return alder;
    }

    /**
     * Begrænset udlad revideringer med scope = this
     * @param undladUdvalgteRevideringer
     */
    public void setUndladUdvalgteRevideringer(boolean undladUdvalgteRevideringer) {
        this.undladUdvalgteRevideringer = undladUdvalgteRevideringer;
    }

    /**
     * Skal sættes til true hvis klienten ved at den absolut ikke må trigge flytning af dagbogsopgaver
     *
     * @param pDagsbogsflytningDisabled   false er default
     */
    public void setDagsbogsflytningDisabled(boolean pDagsbogsflytningDisabled) {
        this.dagsbogsflytningDisabled = pDagsbogsflytningDisabled;
    }

    @Override
    public RegelsaetType searchExtended(SammenhaengsvalideringTargetFinder searchType) {
        super.searchExtended(searchType);
        RegelsaetType typeToSearch = searchType.getSearchType();

        if (typeToSearch != null && typeToSearch instanceof Daekningstypekategori) {
            handleDaekningstypeKategori(searchType);
            searchType.setResultObject(typeToSearch);
            return typeToSearch;
        }
        return null;
    }

    private void handleDaekningstypeKategori(SammenhaengsvalideringTargetFinder searchType) {
        final Sammenhaengsvalideringsregler reglen = searchType.getSammenhaengsvalideringsreglen();
        if (reglen.getSammenhaeng() == Sammenhaengsvalideringsregler.Sammenhaeng.TILLADER) {
            if (selectableFiltre != null)
                selectableFiltre.remove(reglen.getTypeTil());
        } else {
            if (selectableFiltre == null)
                selectableFiltre = new HashSet<>();
            selectableFiltre.add((Daekningstypekategori) reglen.getTypeTil());
        }
    }


	public boolean isVisesPaaMineSider() {
		String egenskabVaerdi = getEgenskabVaerdi(Aftaleegngrp.Aftalefelt.VISMINESID.getKortBenaevnelse());
		if(egenskabVaerdi != null) {
			if(egenskabVaerdi.equals(GaiaConst.JA_TEXT)) {
				return true;
			}
		}
		return false;
	}

}