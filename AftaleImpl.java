package dk.gensam.gaia.model.aftale;

import com.objectmatter.bsf.AggregateOQuery;
import com.objectmatter.bsf.BOReferenceNotUpdatedException;
import com.objectmatter.bsf.FilterQuery;
import com.objectmatter.bsf.OQuery;
import dk.gensam.gaia.business.*;
import dk.gensam.gaia.business.bonusregulering.BonusForloebFiktiv;
import dk.gensam.gaia.business.bonusregulering.BonusreguleringsHelper;
import dk.gensam.gaia.business.bonusregulering.BonustypeInfo;
import dk.gensam.gaia.business.rpn.RPNService;
import dk.gensam.gaia.business.rpn.RPNServiceImpl;
import dk.gensam.gaia.funktion.totalkunde.TotalkundetypeRegelManager;
import dk.gensam.gaia.integration.informationssystem.UdtraekInformationssystemAftale;
import dk.gensam.gaia.integration.naersikring.OpfoelgAftaleOverfoerImpl;
import dk.gensam.gaia.integration.sms.Sms;
import dk.gensam.gaia.integration.sms.SmsStatus;
import dk.gensam.gaia.model.MSIntegrationV2.Brev;
import dk.gensam.gaia.model.adresse.*;
import dk.gensam.gaia.model.afregning.*;
import dk.gensam.gaia.model.anonymisering.AnonymiseringsFrister.AnonymiseringsFristType;
import dk.gensam.gaia.model.anonymisering.AnonymiseringsFristerImpl;
import dk.gensam.gaia.model.crm.OpfoelgningCRMAfgang;
import dk.gensam.gaia.model.dataudsoegning.adresse.Matrikelnummer;
import dk.gensam.gaia.model.dmr.DMRMeddelelseDb;
import dk.gensam.gaia.model.egenskabsystem.*;
import dk.gensam.gaia.model.gebyr.Gebyr;
import dk.gensam.gaia.model.gebyr.GebyrImpl;
import dk.gensam.gaia.model.gsxml.FasteTekster;
import dk.gensam.gaia.model.gsxml.XMLSerialiseringsData;
import dk.gensam.gaia.model.henvisning.HenvisningAftale;
import dk.gensam.gaia.model.henvisning.HenvisningAftaleImpl;
import dk.gensam.gaia.model.individ.Individ;
import dk.gensam.gaia.model.individ.IndividImpl;
import dk.gensam.gaia.model.individ.RelationsHolderRelationIF;
import dk.gensam.gaia.model.klausuler.BttpAftale;
import dk.gensam.gaia.model.klausuler.BttpAftaleAnnulleredeImpl;
import dk.gensam.gaia.model.klausuler.KlausulholderKlausultype;
import dk.gensam.gaia.model.minpraemie.AftaleMinPrae;
import dk.gensam.gaia.model.minpraemie.AftaleMinPraeAnnulleredeImpl;
import dk.gensam.gaia.model.minpraemie.PdPraemieJusteringIF;
import dk.gensam.gaia.model.notesdataudsoegning.Notesdokumenttype;
import dk.gensam.gaia.model.notesdataudsoegning.NotesdokumenttypeImpl;
import dk.gensam.gaia.model.omkost.*;
import dk.gensam.gaia.model.omraade.AfOrRisikosted;
import dk.gensam.gaia.model.omraade.AftaleOmraade;
import dk.gensam.gaia.model.omraade.Omraade;
import dk.gensam.gaia.model.omraade.ReasProduktAdresse;
import dk.gensam.gaia.model.opgave.dagbog.*;
import dk.gensam.gaia.model.opgave.regulering.AftpRgtp;
import dk.gensam.gaia.model.opgave.regulering.Reguleringstype;
import dk.gensam.gaia.model.opgave.revidering.AftaleRev;
import dk.gensam.gaia.model.overblik.OverblikHaendelseManuel;
import dk.gensam.gaia.model.overblik.OverblikHaendelseManuelImpl;
import dk.gensam.gaia.model.pbs.*;
import dk.gensam.gaia.model.print.PrintjobPersistensService;
import dk.gensam.gaia.model.provision.ForsikringstagerAftalehaendelseProvisionmodtager;
import dk.gensam.gaia.model.provision.ForsikringstagerAftalehaendelseProvisionmodtagerImpl;
import dk.gensam.gaia.model.provision.Provision;
import dk.gensam.gaia.model.provision.beregn.ProvisionsHaendelsesMapper;
import dk.gensam.gaia.model.provision.regel.BrugerProvisionsmodtagerImpl;
import dk.gensam.gaia.model.rabat.*;
import dk.gensam.gaia.model.reassurance.*;
import dk.gensam.gaia.model.relationer.*;
import dk.gensam.gaia.model.rpn.RPNAftaleKode;
import dk.gensam.gaia.model.rpn.RPNRegulering;
import dk.gensam.gaia.model.rykker.RykkerKonsekvensRegelImpl;
import dk.gensam.gaia.model.rykker.Rykkerkonsekvens;
import dk.gensam.gaia.model.rykker.RykkerkonsekvensOmfang;
import dk.gensam.gaia.model.rykker.SkadeBlokering;
import dk.gensam.gaia.model.skade.*;
import dk.gensam.gaia.model.sms.SmsMO;
import dk.gensam.gaia.model.tilafgang.Opsigelse;
import dk.gensam.gaia.model.tilafgang.OpsigelseImpl;
import dk.gensam.gaia.model.tilafgang.Opsigelsesoplysning;
import dk.gensam.gaia.model.totalkunde.AftaleTotalkundetype;
import dk.gensam.gaia.model.totalkunde.TotalkundetypeAftaletype;
import dk.gensam.gaia.model.totalkunde.TotalkundetypeIndivid;
import dk.gensam.gaia.model.totalkunde.TotalkundetypeIndividImpl;
import dk.gensam.gaia.model.udsendtedokumenter.ReguleringsskemaStatus;
import dk.gensam.gaia.model.util.*;
import dk.gensam.gaia.model.wakeup.Wakeup;
import dk.gensam.gaia.model.wakeup.Wakeup.WakeupType;
import dk.gensam.gaia.model.wakeup.WakeupImpl;
import dk.gensam.gaia.model.ydelse.*;
import dk.gensam.gaia.util.*;
import dk.gensam.gaia.util.dato.Periode;
import dk.gensam.gaia.util.dbserver.*;
import dk.gensam.gaia.util.sortering.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

public class AftaleImpl extends AftalekomponentImpl implements Aftale, UnikId, Serializable, IdentifikationsLabelHolder, KontodimensionIF {
	private static Logger log_= LoggerFactory.getLogger(AftaleImpl.class);

	// Tilføjelser - start
	public static final String tabelnavn = "AFTALE";
	// Tilføjelser - slut

	// attributes
	public String aftaleId;
	public String TilbydesAfIndivid;
	public String TegnesAfIndivid;

	/**
	 * Initieres af Vbsf som en del af instantieringen og som aflæst via subQuery i selectstatement<br>
	 * SQL command AftaleIsTilbud<p>
	 * 
	 * 2 = har aftalestatus <> TILBUD%
	 * 1 = har kun aftalestatus like TILBUD%
	 * null = har ingen aftalestatus
	 * 
	 * Må ikke forsøges at sat ved insert / update.
	 */
	private Integer aftaleKategori;
	
	// attributter fra join-files
	public String typeId; // fremmednøglen til Aftaletype fra AftaleAftaletype

//	private BigDecimal bonusRykningsDato_ = null;

	public String nyesteLabel = "";
	public BigDecimal nyesteLabelGld = GaiaConst.NULBD;
	public BigDecimal nyesteLabelOph = GaiaConst.NULBD;

	private boolean isTariferet_ = false;

	private boolean maaGentegnes = true; // Som udgangspunkt på en aftale gentenges. Ved flyt aftale anvendes denne
	// attribut = false (DBFactory.txcommit)

	private String kontodimension = null;
	private Egenskab kontodimensionEgenskab = null;
	private boolean statusKoerselMaxAfvDage = false;
	private boolean undladKorttidsRevidering = false;
	private boolean isRykkerKoerselIgang_ = false;
	
	private boolean isMedRistornoregler = false;
    private boolean isMedRistornoreglerChecket = false;

	public void reset() {
		aftaleId = GaiaConst.TOMSTRING;
		oprDato = GaiaConst.NULBD;
		oprTid = GaiaConst.NULBD;
		oprBruger = GaiaConst.TOMSTRING;
		revDato = GaiaConst.NULBD;
		revTid = GaiaConst.NULBD;
		revBruger = GaiaConst.TOMSTRING;
		gld = GaiaConst.NULBD;
		oph = GaiaConst.NULBD;
		TilbydesAfIndivid = GaiaConst.TOMSTRING;
		TegnesAfIndivid = GaiaConst.TOMSTRING;
		typeId = GaiaConst.TOMSTRING;
	}
	
	@Override
	public String dumpInstance() {
		StringBuilder sb = new StringBuilder("[" + aftaleId + "] " +toString() + " ");
		sb.append(this.getGldOphLabel() + " ");
		sb.append(" Opr: "+this.getOpr() + " Rev: " + this.getRev() + " " );
		sb.append("Individ=" + TegnesAfIndivid + " ");
		sb.append(this.getAftaleStatustypeNyesteUdenOphoer().getStatustype().getBenaevnelse());
	    return sb.toString();
    }

	public AftaleImpl() { /* NOP */
	}

	public AftaleImpl(String id) {
		this.setTomtObjekt(true);
		this.setId(id);
	}

	public String getAftaleId() {
		return aftaleId;
	}

	public boolean isBlokeret(BigDecimal pDato) {

		AftaleStatustype[] afSttpTab = this.getAftaleStatustypeUdenOphoer();
		if (afSttpTab != null) {
			for (AftaleStatustype aftaleStatustype : afSttpTab) {
				if (aftaleStatustype.getGld().compareTo(pDato) <= 0 && aftaleStatustype.getStatustype().isStoptype()) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * returnerer true, hvis aftalen har en stoptype pr. ikrafttrædelse 
	 * eller
	 * returneres false
	 */
	public boolean isBlokeretPrIkraft() {

		AftaleStatustype[] afSttpTab = this.getAftaleStatustypeUdenOphoer();
		if (afSttpTab != null) {
			for (AftaleStatustype aftaleStatustype : afSttpTab) {
				if (aftaleStatustype.getGld().compareTo(this.getGld()) == 0 && aftaleStatustype.getStatustype().isStoptype()) {
					return true;
				}
			}
		}

		return false;
	}

	public void setAftaleId(String pAftaleId) {
		aftaleId = pAftaleId;
	}

	// Reference accessors & mutators

	public IndividImpl getTilbydesAfIndivid() {
		return DBServer.getInstance().getVbsf().lookup(IndividImpl.class, TilbydesAfIndivid);
	}

	public IndividImpl getTegnesAfIndivid() {
		// if (ino_ == null)
		return DBServer.getInstance().getVbsf().lookup(IndividImpl.class, TegnesAfIndivid);
		// return ino_;
	}

	public String getTegnesAfIndividId() {
		return TegnesAfIndivid;
	}

	public Object[] getAftalesAftp() {
		throw new UnsupportedOperationException("Programfejl: Aftales Aftaletype skal udledes af attribut ");
	}

	public void addAftaleOmkostningFritagelse(AftaleOmkFritagelse pAftaleOmkfritagelse) {
		PersistensService.addToCollection(this, "AftaleOmkFritagelse", pAftaleOmkfritagelse);
	}

	public void removeAftaleOmkostningFritagelse(AftaleOmkFritagelse pAftaleOmkfritagelse) {
		PersistensService.removeFromCollection(this, "AftaleOmkFritagelse", pAftaleOmkfritagelse);
	}
	public AftaleOmkostningstype getAftaleOmkostningstype(Omkostningstype pOmkostningstype) {
		AftaleOmkostningstype[] alle = getAftaleOmkostningstype();
		for (int i = 0; alle != null && i < alle.length; i++) {
			if (alle[i].getOmkostningstype().equals(pOmkostningstype))
				return alle[i];
		}
		return null;
	}
	public AftaleOmkostningstype[] getAftaleOmkostningstype() {
		return (AftaleOmkostningstype[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmkostningstype");
	}

	public AftaleOmkostningstype[] getAftaleOmkostningstype(BigDecimal pDato) {
		return (AftaleOmkostningstype[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmkostningstype", pDato);
		// DBFactory.get(AftaleOmkostningstype, this, pDato);
	}

	public AftaleOmkFritagelse[] getAftaleOmkFritagelse() {
		return (AftaleOmkFritagelse[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmkFritagelse");
	}

	public AftaleOmkFritagelse[] getAftaleOmkFritagelse(BigDecimal pDato) {
		return (AftaleOmkFritagelse[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmkFritagelse", pDato);
	}

	public boolean getMaaGentegnes() {
		return maaGentegnes;
	}

	/**
	 * Hvis aftalen ikke må gentegnes efter behandling, kan maaGentegnes sættes til false.
	 */
	public void setMaaGentegnes(boolean pMaaGentegnes) {
		maaGentegnes = pMaaGentegnes;
	}
	/**
	 * Returnere alle aftale opsigelser
	 * 
	 * @return Opsigelse[] Samtlige opsigelser
	 */
	public Opsigelse[] getOpsigelseAlle() {
		return getOpsigelseQry(null);
	}
	/**
	 * @param OQuery eller null for alle
	 * @return Opsigelse[] der opfylder det angivne qry eller alle hvis null-arg
	 */
	private Opsigelse[] getOpsigelseQry(OQuery pQry) {
		if (pQry != null)
			return (Opsigelse[]) DBServer.getInstance().getVbsf().get(this, "Opsigelse", pQry );
		return (Opsigelse[]) DBServer.getInstance().getVbsf().get(this, "Opsigelse" );

		/*
		 * ArrayList liste = new ArrayList(); AftaleOpsigelse[] ops = getAftaleOpsigelser(); if(ops != null &&
		 * ops.length > 0){ for(int i=0; i<ops.length; i++){ // Der er inkonsistens i databaserne d.v.s. at der finder
		 * AftaleOpsigenser der peger på en opsigelse der ikke findes. if(ops[i].getOpsigelse() != null)
		 * liste.add(ops[i].getOpsigelse()); } return (Opsigelse[])ContainerUtil.toArray(liste); } return null;
		 */
	}

	public Opsigelse[] getFremtidigeOpsigelser(BigDecimal pDato) {
		Opsigelse[] alleOpsigelser = getOpsigelseAlle();
		if (alleOpsigelser != null) {
			return (Opsigelse[]) Datobehandling.findFremtidige(alleOpsigelser, pDato);
		}
		return null;
	}

	public boolean hasEdiOpsigelserGldFremtidige(BigDecimal pDato) {
		Opsigelse[] opsigelseAlle = getOpsigelseAlle();
		if (opsigelseAlle != null) {
			for (Opsigelse ops : opsigelseAlle) {
				if (ops.getOpsigelsesdato().compareTo(pDato) >= 0) {
					Opsigelsesoplysning opsigelsesoplysning = ops.getOpsigelsesoplysning();
					if (opsigelsesoplysning != null && opsigelsesoplysning.hasOpsigelsesOplysningExtended())
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returnere den gældende (ikke fremtidige) opsigelse som er registreret på aftalen.
	 * 
	 * @return Opsigelse Den gældende opsigelse, null hvis ingen opsigelser fundet eller kun fremtidige
	 */
	public Opsigelse getOpsigelseGld(BigDecimal pDato) {
		Opsigelse[] ops2 = this.getOpsigelseQry(OpsigelseImpl.getOQueryOrdered(pDato, this));
		// returnerer 0 eller 1 instans 
		if (ops2 == null)
			return null;
		if (ops2[0].getOpsigelsesdato().compareTo(pDato)==0)
			return ops2[0];
//
//		Opsigelse[] ops = getOpsigelseAlle();
//		if (ops == null || ops.length < 1)
//			return null;
//
//		if (ops.length > 1)
//			ReflectionSort.sort(ops, "getSortKriterie", ReflectionSort.SORTERING_FALDENDE);

//		for (int i = 0; i < ops.length; i++) {
//			if (ops[i].getOpsigelsesdato().compareTo(pDato) == 0)
//				return ops[i];
//		}
		// Hvis en aftale er sat til ophør i CUA ligger opsigelsesoplysningerne på ophørs datoen.
		if (pDato.compareTo(Datobehandling.datoPlusMinusAntal(getOph(), 1)) == 0) {
			if (ops2[0].getOpsigelsesdato().compareTo(getOph()) == 0)
				return ops2[0];
		}
		return null;
	}

	/**
	 * Returnere den nyeste opsigelse som er registreret på aftalen.
	 * 
	 * @return Opsigelse Den nyeste opsigelse, null hvis ingen opsigelser fundet
	 */
	public Opsigelse getOpsigelseNyeste() {
		Opsigelse[] ops2 = this.getOpsigelseQry(OpsigelseImpl.getOQueryOrdered(null, this));
		return ops2 != null ? ops2[0] : null;
	}

	public List<Opsigelse> getOpsigelserNyeste() {
		List<Opsigelse> nyesteOpsigelser = new ArrayList<>();
		Opsigelse[] opsigelser = this.getOpsigelseQry(OpsigelseImpl.getOQueryOrdered(null, null, this));
		if (opsigelser != null) {
			nyesteOpsigelser.add(opsigelser[0]);
			if (opsigelser.length > 1) {
				BigDecimal nyesteDato = opsigelser[0].getOpsigelsesdato();
				for (int i = 1; i < opsigelser.length; i++) {
					if (nyesteDato.compareTo(opsigelser[i].getOpsigelsesdato()) == 0) {
						nyesteOpsigelser.add(opsigelser[i]);
					} else {
						break;
					}
				}
			}
		}
		return nyesteOpsigelser;
	}

    /** Finder EDI opsigelserne ud fra alle opsigeler **/
    public List<Opsigelse> getEDIOpsigelser() {
        List<Opsigelse> ediOpsigelser = new ArrayList<>();
        Opsigelse[] opsigelseAlle = getOpsigelseAlle();
        if (opsigelseAlle != null) {
            for (Opsigelse opsigelse : opsigelseAlle) {
                Opsigelsesoplysning opsigelsesoplysning = opsigelse.getOpsigelsesoplysning();
                if (opsigelsesoplysning != null && opsigelsesoplysning.hasOpsigelsesOplysningExtended()) {
                    // vi vil kun have edi-opsigelser
                    ediOpsigelser.add(opsigelse);
                }
            }
        }
        return ediOpsigelser;
    }

    /**
	 * @param pDato
	 * @return true hvis aftalen har en aktive status "Afv.ændr." eller "Godk.ændr." pr. pDato,
     * eller hvis aftalen er ophørt før reguleringsdato
     * Ellers false.
	 */
	public boolean harStatusAendring(BigDecimal pDato){
		boolean aendringStatusFundet = false;
		AftaleStatustype[] aftaleStatustyper = getAftaleStatustyperMedGld(pDato);
		if (aftaleStatustyper != null){
			for (AftaleStatustype afsttp : aftaleStatustyper){
				String sttpKortBnv = afsttp.getStatustype().getKortBenaevnelse().trim();
				if (sttpKortBnv.equals(Statustype.AFVAENDR) || sttpKortBnv.equals(Statustype.STATUS_TYPE_GODKENDT_AENDRING)) {
					if (afsttp.getOph().compareTo(BigDecimal.ZERO) == 0) {
						aendringStatusFundet = true;
					}
				}
			}
		}
		// ophørte er også reguleringer
        if(!aendringStatusFundet && this.isOphoert(pDato)) {
		    if(harStatusAendringVedOphortForsikring()) return true;
        }

		return aendringStatusFundet;
	}

    protected boolean harStatusAendringVedOphortForsikring() {
        List<AftaleStatustype> aftaleStatustypes = ContainerUtil.asList(this.getAftaleStatustypeUdenOphoer());
        Optional<AftaleStatustype> klarAendringer = aftaleStatustypes
                .stream().filter(s -> s.getStatustype().getKortBenaevnelse().trim().compareTo(Statustype.STATUS_TYPE_GODKENDT_AENDRING) == 0 ||
                        s.getStatustype().getKortBenaevnelse().trim().compareTo(Statustype.AFVAENDR) == 0)
                .findAny();
        return klarAendringer.isPresent();
    }

    public List<AftaleRegDatoAfregning> getAftaleRegDatoAfregningUdenStatusAendring(){
		List<AftaleRegDatoAfregning> aftaleRegDatoAfregningUdenStatusAendring = new ArrayList<>();
		AftaleRegDatoAfregning[] aftaleRegDatoAfregning = this.getAftaleRegDatoAfregning();
		if (aftaleRegDatoAfregning != null){
			for (AftaleRegDatoAfregning afRegDatoAr : aftaleRegDatoAfregning){
				if (afRegDatoAr.getAfregning() == null){
					BigDecimal aendringsdato = afRegDatoAr.getBagudperiodesSlutdato();
					aendringsdato = Datobehandling.datoPlusMinusAntal(aendringsdato, 1);
					if (!this.harStatusAendring(aendringsdato)){
						aftaleRegDatoAfregningUdenStatusAendring.add(afRegDatoAr);
					}
				}
			}
		}

		return aftaleRegDatoAfregningUdenStatusAendring;
	}
	/**
	 * @param pStatustype null = return null
	 * @param pInclOphoerte
	 * 
	 * @return alle AftaleStatustype der matcher den givne Statustype.
	 */
	public AftaleStatustype[] getAftaleStatustype(Statustype pStatustype, boolean pInclOphoerte) {
		if (pStatustype != null) {
			OQuery qry = new OQuery();
			qry.add(pStatustype.getId(), "Statustype");
			if (!pInclOphoerte)
				qry.add(BigDecimal.ZERO, "getOph");
			
			return getAftaleStatustype(qry);
		}
		return null;
	}
	/**
	 * @param pFilter i form af OQuery, null = alle
	 * 
	 * @return alle AftaleStatustype der matcher den givne OQuery.
	 */
	public AftaleStatustype[] getAftaleStatustype(OQuery pFilter) {
		if (pFilter != null) {
			AftaleStatustype[] aftaleStatustypes = (AftaleStatustype[]) DBServer.getInstance().getVbsf().get(this, AftaleStatustype.AFTALESTATUSTYPE, pFilter);
			return aftaleStatustypes;
		}
		return (AftaleStatustype[]) DBServer.getInstance().getVbsf().get(this, AftaleStatustype.AFTALESTATUSTYPE);
	}
	/**
	 * @param pDiscardAftaleStatusTypeIndenQry - hvis true Discardes AftalestatustypeImpl inden qry
	 * kalder videre til AftaleStatustype[] getAftaleStatustype(OQuery pFilter) med null i pFilter
	 * @return alle AftaleStatustype der matcher den givne OQuery.
	 */
	public AftaleStatustype[] getAftaleStatustype(boolean pDiscardAftaleStatusTypeIndenQry) {
		if(pDiscardAftaleStatusTypeIndenQry) {
			DBServer.getInstance().getVbsf().discardAll(AftaleStatustypeImpl.class);
		}
		return (getAftaleStatustype(null));
    }
	public AftaleStatustype[] getAftaleStatustypeAktuelle(BigDecimal pDato) {
		OQuery q = new OQuery();
		q.add(GaiaConst.NULBD, "oph", OQuery.EQUAL);
		q.add(pDato, "gld", OQuery.LESS_OR_EQUAL);
		q.addOrder("gld", OQuery.DESC);
		q.setMaxCount(1);
		AftaleStatustype[] aftaleStatustype = this.getAftaleStatustype(q);

		if(aftaleStatustype != null && aftaleStatustype.length > 0) {
			q = new OQuery();
			q.add(GaiaConst.NULBD, "oph", OQuery.EQUAL);
			q.add(aftaleStatustype[0].getGld(), "gld", OQuery.EQUAL);
			return this.getAftaleStatustype(q);
		}
		return null;
	}

	/**
	 * 
	 * @return alle AftaleStatustype uden ophørsdato.
	 */
	public AftaleStatustype[] getAftaleStatustypeUdenOphoer() {
		OQuery q = new OQuery();
		q.add(GaiaConst.NULBD, "oph", OQuery.EQUAL);
		return this.getAftaleStatustype(q);
	}

	/**
	 * OBS: ikke en normal datobegrænsning !
	 * 
	 * @return alle AftaleStatustype præcist med den givne gældende.
	 */
	public AftaleStatustype[] getAftaleStatustyperMedGld(BigDecimal pGld) {
		OQuery q = new OQuery();
		q.add(pGld, "gld", OQuery.EQUAL);
		return this.getAftaleStatustype(q);
	}

	/**
	 * OBS: ikke en normal datobegrænsning !
	 * 
	 * @return senestoprettede AftaleStatustype præcist med den givne gældende og uden ophør.
	 */
	public AftaleStatustype getAftaleStatustypenMedGld(BigDecimal pGld) {
		OQuery q = new OQuery();
		q.add(pGld, "gld", OQuery.EQUAL, OQuery.AND);
		q.add(GaiaConst.NULBD, "oph", OQuery.EQUAL, OQuery.AND);
		q.addOrder("oprDato", OQuery.DESC);
		q.addOrder("oprTid", OQuery.DESC);
		q.setMaxCount(1);
		AftaleStatustype[] o = this.getAftaleStatustype(q);
		return (o != null ? o[0] : null);
	}

	public AftaleStatustype getAftaleStatustypeSenestOprettetMedGld(BigDecimal pGld) {
		OQuery q = new OQuery();
		q.add(pGld, "gld", OQuery.EQUAL, OQuery.AND);
		q.addOrder("oprDato", OQuery.DESC);
		q.addOrder("oprTid", OQuery.DESC);
		q.setMaxCount(1);
		AftaleStatustype[] o = this.getAftaleStatustype(q);
		return (o != null ? o[0] : null);
	}

	/**
	 * Returnere alle de statustyper som skal sættes til ophør. Metoden finder alle de aftalestatustyper som ikke har en
	 * ophørsdato og som har gældende på eller efter den modtaget parameter dato.
	 */
	public AftaleStatustype[] getAftaleStatusTyperTilOphoer(BigDecimal pDato) {

		AftaleStatustype[] alleTyper = getAftaleStatustypeUdenOphoer();
		List<AftaleStatustype> typerTilOphoer = new ArrayList<AftaleStatustype>();

		if (alleTyper != null && alleTyper.length > 0) {
			for (int t = 0; t < alleTyper.length; t++) {
				AftaleStatustype type = alleTyper[t];
				if (type != null) {
					// Hvis aftalestatustypens gældende er >= pDato OG aftalestatustypen IKKE har en ophørsdato...
					if (type.getGld().compareTo(pDato) >= 0 && type.getOph().intValue() == 0) {
						typerTilOphoer.add(type); // Tilføj typen til listen af de typer som skal sættes til ophør.
					}
				}
			}
		}
		return ContainerUtil.toArray(typerTilOphoer);
	}

	/**
	 * Returnere alle de statustyper som skal annulleres. Metoden finder alle de aftalestatustyper som ikke har en
	 * ophørsdato og som har gældende på eller efter den modtaget parameter dato.
	 */
	public AftaleStatustype[] getAftaleStatusTyperTilAnnullering() {
		AftaleStatustype[] alleTyper = getAftaleStatustypeUdenOphoer();
		List<AftaleStatustype> typerTilOphoer = new ArrayList<AftaleStatustype>();

		if (alleTyper != null && alleTyper.length > 0) {
			for (int t = 0; t < alleTyper.length; t++) {
				AftaleStatustype type = alleTyper[t];
				if (type != null) {
					if (type.getOph().intValue() == 0) {
						typerTilOphoer.add(type); // Tilføj typen til listen af de typer som skal sættes til
						// annulleres
					}
				}
			}
		}
		return ContainerUtil.toArray(typerTilOphoer);
	}

	// /**
	// Returner gældende Aftalestatustype
	// OBS kanv være fremtidig
	// OBS det angivne dato-argument anvendes ikke
	// Anvend evt. metoden uden dato-argument direkte
	// */
	// private AftaleStatustype getAftaleStatustypezzz(BigDecimal pGaeldendeDato) {
	// // vi anvender ikke længere opgørelsesdato
	// return getAftaleStatustype();
	//
	// /*
	// AftaleStatustype[] temp = getAftaleStatustyper(pGaeldendeDato);
	// if (temp != null && temp.length > 0){
	// if(temp.length==1)
	// return k[0];
	// for(int i=0; i<temp.length; i++){
	// if(temp[i].getOph().intValue()==0){
	// return temp[i];
	// }
	// }
	// return temp[0];
	// }
	// return null; */
	// }

	/**
	 * tjek om aftalen indeholder en statustype hvor gld-dato er lig med aftalens ophørsdato + 1 dag og hvor
	 * statustypeforkortelse er lig med "KLAROPHØR". Denne metode bruges når der dannes til- og afgangslister
	 * 
	 * @return true hvis aftalen har en ophørsdato og har KLAROPHØR pr. dagen efter ellers false
	 */
	public boolean findesStatustypeKlarophoer() {
		if (!this.isOphUdfyldt()) {
			return false;
		}
		AftaleStatustype[] as = getAftaleStatustyperMedGld(Datobehandling.datoPlusMinusAntal(getOph(), 1));

		if (as != null) {
			for (int i = 0; i < as.length; i++) {
				// hvis kortbenævnelsen er lig med "KLAROPHØR" skal der returneres true
				if (as[i].getStatustype().getKortBenaevnelse().trim().equals(Statustype.KLAROPHOER)) {
					return true;
				}
			}
		}
		// der blev ikke fundet en gyldig statustype, returner false
		return false;
	}
	
	/**
	 * 
	 * @return true hvis aftalen har har STATUS_TYPE_AFV_GODKENDT pr. gældende dato  ellers false
	 */
	public boolean findesStatustypeAfventGodkendt() {
		return findesStatustype(Statustype.STATUS_TYPE_AFV_GODKENDT);
	}
	
	/**
	 * 
	 * @return true hvis aftalen har har STATUS_TYPE_GODKENDT pr. gældende dato  ellers false
	 */
	public boolean findesStatustypeGodkendt() {
		return findesStatustype(Statustype.STATUS_TYPE_GODKENDT);
	}
	
	/**
	 * @param pStatustype Statustypens korte benævnelse 
	 * @return true hvis aftalen har har STATUS_TYPE_GODKENDT pr. gældende dato  ellers false
	 */
	public boolean findesStatustype(String pStatustype) {
		AftaleStatustype[] as = getAftaleStatustyperMedGld(getGld());
		
		if (as != null) {
			for (int i = 0; i < as.length; i++) {
				if (!as[i].isOphoert() &&
						as[i].getStatustype().getKortBenaevnelse().trim().equals(pStatustype)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sikrer refresh/reload af aftalens statustyper
	 */
	public void refreshAftaleStatustype() {
		DBServer.getInstance().getVbsf().markCollectionDirty(this, AftaleStatustype.AFTALESTATUSTYPE);
	}
	/**
	 * Finder den Aftalestatustype der bør anvendes ved læsning i dag ! Accepterer altså ikke en opgørelsesdato jf.
	 * gældende regler for visning af status 
	 * 
	 * @return AftaleStatustype uden non-persistente oplysninger om andre statusoplysninger
	 */
	public AftaleStatustype getAftaleStatustypeNyesteUdenOphoer() {
		return getAftaleStatustypeNyesteUdenOphoer(false);
	}
	
	/**
	 * Finder den Aftalestatustype der bør anvendes ved læsning i dag ! Accepterer altså ikke en opgørelsesdato jf.
	 * gældende regler for visning af status 
	 *
	 * @param pDekoreret true = efter kaldet kan metoderne getAntGld og getFlereSttp aflæses, false = det kan de ikke
	 * @return AftaleStatustype
	 */
	public AftaleStatustype getAftaleStatustypeNyesteUdenOphoer(boolean pDekoreret) {
		FilterQuery q = new FilterQuery();
		q.add(GaiaConst.NULBD, "oph", OQuery.EQUAL);
		q.addOrder("gld", OQuery.DESC);
		q.addOrder("oprDato", OQuery.DESC);
		q.addOrder("oprTid", OQuery.DESC);
		q.setMaxCount(1);
		q.setFilterQuery(BigDecimal.ZERO, FilterQuery.GLD); // Lidt snyd med type, men det virker...
		AftaleStatustype[] afsttp = this.getAftaleStatustype(q);
		if (afsttp == null || afsttp.length < 1)
			return null;

		if (!pDekoreret) {
			return afsttp[0];
		}
		boolean flereSttp = false;
		int antGld = 0;
		
		AggregateOQuery aq = new AggregateOQuery(AftaleStatustypeImpl.class);
		aq.add(this.getId(), "Aftale");
		aq.add(GaiaConst.NULBD, "oph", OQuery.EQUAL);
		aq.add(StatustypeImpl.getStatusType(Statustype.IKRAFT).getId(), "Statustype", OQuery.NOT_EQUAL);
		aq.addAggregate("Statustype", AggregateOQuery.COUNT, true); // Count distinct statustype
		aq.addAggregate("*", AggregateOQuery.COUNT); // Count aftalestatustype
		List<List<Object>> results = QueryService.getValuesAsListsInList(aq);
		if (results != null && !results.isEmpty()){
			Integer c1 = (Integer)results.get(0).get(0);
			flereSttp = c1 != null && c1 > 1;
			Integer c2 = (Integer)results.get(0).get(1);
			antGld = c2 != null ? c2 : 0; 

		}
		int returnIx = 0;
		afsttp[returnIx].setAntGld(antGld);
		afsttp[returnIx].setFlereSttpGld(flereSttp);
		return afsttp[returnIx];
	}

	public AftaleStatustype getAftaleStatustypeSenestOprettedeFoerDato(BigDecimal pDato) {
		AftaleStatustype[] afsttp = this.getAftaleStatustypeSenestOprettedeFoerDato(pDato, 1);
		if (afsttp == null || afsttp.length < 1)
			return null;
		return afsttp[0];
	}

	public AftaleStatustype getAftaleStatustypeSenestOprettedeFoerDatoExclTilbudSttp(BigDecimal pDato) {
		AftaleStatustype[] afsttp = this.getAftaleStatustypeSenestOprettedeFoerDato(pDato, 0);
		if (afsttp == null || afsttp.length < 1)
			return null;
		// Den første der ikke er en tilbud-type
		for (int i = 0; i < afsttp.length; i++) {
			if (!(afsttp[i].getStatustype().isTilbud())) {
				return afsttp[i];
			}
		}
		return null;
	}

	private AftaleStatustype[] getAftaleStatustypeSenestOprettedeFoerDato(BigDecimal pDato, int pMaxCount) {
		FilterQuery q = new FilterQuery();
		q.add(pDato, "oprDato", OQuery.LESS);
		q.addOrder("gld", OQuery.DESC);
		q.addOrder("oprDato", OQuery.DESC);
		q.addOrder("oprTid", OQuery.DESC);
		if (pMaxCount > 0) {
			q.setMaxCount(pMaxCount);
		}
		q.setFilterQuery(BigDecimal.ZERO, FilterQuery.GLD); // Lidt snyd med type, men det virker...
		return this.getAftaleStatustype(q);
	}

	public AftaleArtp getAftaleArtp(BigDecimal pGaeldendeDato) {
		AftaleArtp[] rels = (AftaleArtp[])DBServer.getInstance().getVbsf().get(this, AftaleArtp.AftaleArtpColl, pGaeldendeDato);
		if (rels == null || rels.length == 0) {
			return null;
		} else if(rels != null && rels.length > 1) {
			// Der er set flere på konverterede forsikringer, så her returneres den med den seneste gældende.
			// Feb 2012 testcase har fremprovokeret denne fejl ved aflæsning af collectionen med open transaction
			//    og ucommittede ændringer af afregningstype pr. samme dato
			//    Da databasen ER korrekt i den situation vil et null-resultat af refresh kunne ignoreres
			BigDecimal gld = BigDecimal.ZERO;
			AftaleArtp result = null;
			for (int i = 0; i < rels.length; i++) {
				Object refresh = DBServer.getInstance().getVbsf().refresh(rels[i]);
				if (refresh == null)
					continue;
				if(rels[i].getGld().compareTo(gld) > 0) {
					result = rels[i];
					gld = rels[i].getGld();
				}
			}
			return result;
		}
		return rels[0]; // der kan og må kun være een
	}

	public AftaleArtp[] getAftaleArtpGldOgFremtidige(BigDecimal pGaeldendeDato) {
		AftaleArtp[] o = (AftaleArtp[])DBServer.getInstance().getVbsf().get(this, pGaeldendeDato, AftaleArtp.AftaleArtpColl);
//		Object[] o = Datobehandling.findGaeldendeOgFremtidige(getAftaleArtp(), pGaeldendeDato);
		if (o == null || o.length == 0)
			return null;
		return o;
	}

	/**
	 * Get de fremtidige afregningstyper tilknyttet aftalen
	 */
	public AftaleArtp[] getAftaleArtpFremtidige(BigDecimal pGaeldendeDato) {
		Object[] o = Datobehandling.findFremtidige(getAftaleArtp(), pGaeldendeDato);
		if (o == null || o.length == 0)
			return null;
		return (AftaleArtp[]) o;
	}

	/**
	 * Get gældende Afregningstype tilknyttet aftalen
	 */
	public Afregningstype getAftalensArtp(BigDecimal pGaeldendeDato) {
		AftaleArtp afartp = getAftaleArtp(pGaeldendeDato);
		if (afartp == null) {
			return null;
		}
		return afartp.getAfregningstype();
	}
	
	public boolean isAftaleArtpIkkeOphoertPBS() {
		AftaleArtp aftaleArtp = getAftaleArtpIkkeOphoert();
		if(aftaleArtp != null) {
			return aftaleArtp.getAfregningstype().isPBS();
		}
		return false;
	}

	public AftaleArtp getAftaleArtpIkkeOphoert() {
		OQuery qry = DBServer.getVbsfInst().queryCreate(AftaleArtpImpl.class);
		qry.add(getId(), "Aftale", OQuery.EQUAL);
		qry.add(BigDecimal.ZERO, "oph", OQuery.EQUAL);
		AftaleArtp[] aftaleArtper = (AftaleArtp[]) DBServer.getVbsfInst().queryExecute(qry);
		// Der bør være en og kun en
		if(aftaleArtper != null && aftaleArtper.length == 1) {
			return aftaleArtper[0];
		}
		return null;
	}

	public boolean isPBSTilmeldt(BigDecimal pGaeldendeDato) {
		if (getPBSStatus(pGaeldendeDato) == Aftale.PBSTILMELDT)
			return true;
		return false;
		// men kan godt være på vej
	}

	public boolean isPBSPaaVej(BigDecimal pGaeldendeDato) {
		if (getPBSStatus(pGaeldendeDato) == Aftale.PBSPAAVEJ)
			return true;
		return false;
	}

	public boolean isPBSEjTilmeldt(BigDecimal pGaeldendeDato) {
		if (getPBSStatus(pGaeldendeDato) == Aftale.PBSIKKETILMELDT)
			return true;
		return false;
	}

	/**
	 * Afgører om en aftale er tilmeldt PBS pr. en given dato.
	 */
	private int getPBSStatus(BigDecimal pGaeldendeDato) {
		Afregningstype aftalensArtp = getAftalensArtp(pGaeldendeDato);
		if (aftalensArtp != null && aftalensArtp.isPBS()) {
			if (harPBSAftalenr(pGaeldendeDato))
				return Aftale.PBSTILMELDT;
			else
				return Aftale.PBSPAAVEJ;
		} else
			return Aftale.PBSIKKETILMELDT;
	}

	/**
	 * Afgører om en aftale har PBS aftalenummer fra og med en given dato.
	 */
	public boolean harPBSAftalenr(BigDecimal pGaeldendeDato) {
		return getPBSAftalenrGldEllerFremtidig(pGaeldendeDato) != null;
		
	}
	
	public String getPBSAftalenrGldEllerFremtidig(BigDecimal pGaeldendeDato) {
//		Aftaleegngrp pbsg = AftaleegngrpImpl.getAftaleegngrpPBS();
//		if (pbsg == null)
//			// selskabet anvender ikke PBS
//			return null;
//
//		// DBFactory.queryInit();
//		OQuery qry = QueryService.queryCreate(AftaleAfegnImpl.class);
//		QueryService.queryAdd(qry, this.getId(), "EgenskabHolder", QueryService.EQUAL);
//		QueryService.queryAdd(qry, pbsg.getId(), "EgenskabsgruppeId", QueryService.EQUAL);
//		AftaleAfegn[] pbs = (AftaleAfegn[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		AftaleAfegn[] pbs = getPbsAftaleAfEgskaber(false);
		if (pbs == null || pbs[0] == null)
			return null;
		AftaleAfegn[] o = (AftaleAfegn[])Datobehandling.findGaeldendeOgFremtidige(pbs, pGaeldendeDato);
		if (o != null) {
			if (o.length > 0 && o[0] != null) {
				return o[0].getEgenskab().formatToDisplay();
			}
		}
		return null;
	}
	public String getPBSAftalenrGld (BigDecimal pGaeldendeDato) {
//		Aftaleegngrp pbsg = AftaleegngrpImpl.getAftaleegngrpPBS();
//		if (pbsg == null)
//			// selskabet anvender ikke PBS
//			return null;
//
//		// DBFactory.queryInit();
//		OQuery qry = QueryService.queryCreate(AftaleAfegnImpl.class);
//		QueryService.queryAdd(qry, this.getId(), "EgenskabHolder", QueryService.EQUAL);
//		QueryService.queryAdd(qry, pbsg.getId(), "EgenskabsgruppeId", QueryService.EQUAL);
//		AftaleAfegn[] pbs = (AftaleAfegn[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		AftaleAfegn[] pbs = getPbsAftaleAfEgskaber(false);
		if (pbs == null || pbs[0] == null)
			return null;
		AftaleAfegn[] o = (AftaleAfegn[])Datobehandling.findGaeldende(pbs, pGaeldendeDato);
		if (o != null) {
			if (o.length > 0 && o[0] != null) {
				return o[0].getEgenskab().formatToDisplay();
			}
		}
		return null;
	}

	public String getPBSAftalenrSenesteGld() {
//		Aftaleegngrp pbsg = AftaleegngrpImpl.getAftaleegngrpPBS();
//		if (pbsg == null) {
//			// selskabet anvender ikke PBS
//			return null;
//		}
//
//		// DBFactory.queryInit();
//
//		OQuery qry = QueryService.queryCreate(AftaleAfegnImpl.class);
//		qry.add(this.getId(), "EgenskabHolder", QueryService.EQUAL);
//		qry.add(pbsg.getId(), "EgenskabsgruppeId", QueryService.EQUAL);
//		qry.addOrder("gld", OQuery.DESC);
//		AftaleAfegn[] pbs = (AftaleAfegn[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		AftaleAfegn[] pbs = getPbsAftaleAfEgskaber(true);
		if (pbs != null) {
			if (pbs.length > 0 && pbs[0] != null) {
				return pbs[0].getEgenskab().formatToDisplay();
			}
		}
		pbs = getPbsAftaleAfEgskaberAnnullerede(true);
		if (pbs != null) {
			if (pbs.length > 0 && pbs[0] != null) {
				return pbs[0].getEgenskab().formatToDisplay();
			}
		}
		return null;
	}
	private AftaleAfegn[] getPbsAftaleAfEgskaber (boolean pDecendingOrder) {

		Aftaleegngrp pbsg = AftaleegngrpImpl.getAftaleegngrpPBS();
		if (pbsg == null)
			// selskabet anvender ikke PBS
			return null;

		// DBFactory.queryInit();
		OQuery qry = QueryService.queryCreate(AftaleAfegnImpl.class);
		QueryService.queryAdd(qry, this.getId(), "EgenskabHolder", QueryService.EQUAL);
		QueryService.queryAdd(qry, pbsg.getId(), "EgenskabsgruppeId", QueryService.EQUAL);
		if(pDecendingOrder) {
			qry.addOrder("gld", OQuery.DESC);
		}
		AftaleAfegn[] pbs = (AftaleAfegn[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (pbs == null || pbs[0] == null) {
			return null;
		}
		return pbs;

	}

	private AftaleAfegn[] getPbsAftaleAfEgskaberAnnullerede(boolean pDecendingOrder) {

		Aftaleegngrp pbsg = AftaleegngrpImpl.getAftaleegngrpPBS();
		if (pbsg == null)
			// selskabet anvender ikke PBS
			return null;

		// DBFactory.queryInit();
		OQuery qry = QueryService.queryCreate(AftaleAfegnAnnulleredeImpl.class);
		QueryService.queryAdd(qry, this.getId(), "EgenskabHolder", QueryService.EQUAL);
		QueryService.queryAdd(qry, pbsg.getId(), "EgenskabsgruppeId", QueryService.EQUAL);
		if(pDecendingOrder) {
			qry.addOrder("gld", OQuery.DESC);
		}
		AftaleAfegn[] pbs = (AftaleAfegn[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (pbs == null || pbs[0] == null) {
			return null;
		}
		return pbs;

	}

	public void setTilbydesAfIndivid(IndividImpl pTilbydesAfIndivid) {
		this.TilbydesAfIndivid = pTilbydesAfIndivid.getId();
	}

	public void setTegnesAfIndivid(IndividImpl pTegnesAfIndivid) {
		this.TegnesAfIndivid = pTegnesAfIndivid.getId();
	}

	public void setAftalesAftp(AftalesAftpImpl pAftalesAftp) {
	}

	public void setAftaletype(Aftaletype pAftaletype) {
		typeId = pAftaletype.getId();
		// aftpo_ = (AftaletypeImpl)pAftaletype;
	}

	/**
	 * Get datoen for en aftales første fornyelse efter nytegning. <BR>
	 * Meget ineffektiv metode, bør aflæses via dagbogaftale i stedet.
	 */
	public BigDecimal getFoersteFornyelsesDato() {
		String gld;
		gld = Datobehandling.format(getGld());
		BigDecimal aar = new BigDecimal(gld.substring(6, 10));
		BigDecimal md = new BigDecimal(gld.substring(3, 5));
		if (getAftaleFftpMd(getGld()) != null && getAftaleFftpMd(getGld()).length > 0) {

			BigDecimal forfaldMd = getAftaleFftpMd(getGld())[0].getMaanedsnummeriaaret();
			String ffmd = forfaldMd.toString();
			if (ffmd.length() < 2)
				ffmd = "0" + ffmd;
			if (md.compareTo(forfaldMd) < 0)
				return new BigDecimal(aar.toString() + ffmd + "01");
			else
				return new BigDecimal(aar.add(new BigDecimal("1")).toString() + ffmd + "01");
		}
		return null;
	}

	public AftaleArtp[] getAftaleArtp() {
		return (AftaleArtp[])DBServer.getInstance().getVbsf().get(this, AftaleArtp.AftaleArtpColl);
	}

	public boolean harYdelser() {
		// Hvorfor skal der mon ikke svares pr. en pDato????
		return DBServer.getInstance().getVbsf().getSize(this, AftaleYdtpAng.AftaleYdtpAng) > 0;
	}

	public void addAftaleYdtpAng(AftaleYdtpAngImpl pAftaleYdtpAng) {
		PersistensService.addToCollection(this, AftaleYdtpAng.AftaleYdtpAng, pAftaleYdtpAng);
	}

	public void removeAftaleYdtpAng(AftaleYdtpAngImpl oldAftaleYdtpAng) {
		PersistensService.removeFromCollection(this, AftaleYdtpAng.AftaleYdtpAng, oldAftaleYdtpAng);
	}

	public GenstandYdtpAng[] getGenstandYdtpAng(BigDecimal pDato, QueryService.Datefilters pDateFilter) {
		if (pDato != null) {
			if (pDateFilter.equals(QueryService.Datefilters.GLD))
				return (GenstandYdtpAng[]) DBServer.getInstance().getVbsf().get(this, GenstandYdtpAng.GenstandYdtpAng, pDato);
			if (pDateFilter.equals(QueryService.Datefilters.GLD_FREMTIDIG))
				return (GenstandYdtpAng[]) DBServer.getInstance().getVbsf().get(this, pDato, GenstandYdtpAng.GenstandYdtpAng);
			throw new UnsupportedOperationException("Understøtter kun GLD og GLD_FREMTIDIG p.t.");
		}
		return (GenstandYdtpAng[]) DBServer.getInstance().getVbsf().get(this, GenstandYdtpAng.GenstandYdtpAng);
	}

	@Override
	public List<AftalekompYdtpAngivelseIF> getAftalekompYdtpAngivelseIFAll(BigDecimal pGld, Genstand pGenstand){
		List<AftalekompYdtpAngivelseIF> retur = new ArrayList<AftalekompYdtpAngivelseIF>();
		if (pGenstand == null) {
			/**
			 * Forsikringsniveauet
			 */
			AftalekompYdtpAngivelseIF[] afyd = getYdelsesAngivelser(pGld);
			for (int i = 0;afyd != null && i < afyd.length; i++) {
				retur.add(afyd[i]);
			}
		}
		/**
		 * Genstand
		 */
		GenstandYdtpAng[] gnyd = getGenstandYdtpAng(pGld, QueryService.Datefilters.GLD);
		for (int i = 0;gnyd!= null && i < gnyd.length; i++) {
			Genstand genstand = gnyd[i].getGenstand();
			if (genstand.isOphoert(pGld))
				continue;
			BigDecimal ophBeregnet = genstand.getOphBeregnet(false);
			if (ophBeregnet.intValue() > 0 && ophBeregnet.compareTo(pGld) < 0)
				continue;
			if (gnyd[i].isGld(pGld) &&
					(pGenstand == null || gnyd[i].getGenstand().equals(pGenstand))) {
				retur.add(gnyd[i]);
			}
		}
		/**
		 * Dækning
		 */
		ProduktYdtpAng[] pdyd = getProduktYdtpAng(pGld, QueryService.Datefilters.GLD);
		for (int i = 0;pdyd!= null && i < pdyd.length; i++) {
			Produkt produkt = pdyd[i].getProdukt();
			if (produkt.isOphoert(pGld))
				continue;

			if (pdyd[i].isGld(pGld) &&
					(pGenstand == null || pdyd[i].getProdukt().getGenstand().equals(pGenstand))) {
				retur.add(pdyd[i]);
			}
		}
		return retur;
	}

	/**
	 *
	 * @param pGldFremtidig
	 * @return alle ydelser på this og børn, gældende eller fremtidig pr. datoen
	 */
	public List<AftalekompYdtpAngivelseIF> getAftalekompYdtpAngivelseAllInclFremtidige(BigDecimal pGldFremtidig){
		List<AftalekompYdtpAngivelseIF> retur = new ArrayList<>();
		if (true) {
			/**
			 * Forsikringsniveauet
			 */
			AftalekompYdtpAngivelseIF[] afyd = getYdelsesAngivelserGldOgFremtidige(pGldFremtidig);
			for (int i = 0;afyd != null && i < afyd.length; i++) {
				retur.add(afyd[i]);
			}

		}
		/**
		 * Genstand
		 */
		GenstandYdtpAng[] gnyd = getGenstandYdtpAng(pGldFremtidig, QueryService.Datefilters.GLD_FREMTIDIG);
		for (int i = 0;gnyd!= null && i < gnyd.length; i++) {
			Genstand genstand = gnyd[i].getGenstand();
			if (genstand.isOphoert(pGldFremtidig))
				continue;
			BigDecimal ophBeregnet = genstand.getOphBeregnet(false);
			if (ophBeregnet.intValue() > 0 && ophBeregnet.compareTo(pGldFremtidig) < 0)
				continue;
			retur.add(gnyd[i]);
		}
		/**
		 * Dækning
		 */
		ProduktYdtpAng[] pdyd = getProduktYdtpAng(pGldFremtidig, QueryService.Datefilters.GLD_FREMTIDIG);
		for (int i = 0;pdyd!= null && i < pdyd.length; i++) {
			Produkt produkt = pdyd[i].getProdukt();
			if (produkt.isOphoert(pGldFremtidig))
				continue;

			retur.add(pdyd[i]);
		}
		return retur;
	}

	/**
	 * @param pDato iht. pDateFilter, null = alle
	 * @param pDateFilter QueryService.Datefilters
	 * @return alle ProduktYdtpAng[] der matcher args og excl. afspejling, noargs = alle incl. afspejlinger
	 */
	public ProduktYdtpAng[] getProduktYdtpAng(BigDecimal pDato, QueryService.Datefilters pDateFilter) {
		if (pDato != null && pDateFilter.equals(QueryService.Datefilters.GLD)){
			FilterQuery gldQuery = DBServer.getInstance().getVbsf().getGldQuery(pDato);
			return getProduktYdtpAng(gldQuery);
		}
		if (pDato != null && pDateFilter.equals(QueryService.Datefilters.GLD_FREMTIDIG)){
			FilterQuery gldQuery = (FilterQuery)DBServer.getInstance().getVbsf().getGldFremtidigeQuery(null, pDato);
			return getProduktYdtpAng(gldQuery);
		}
		// TODxxO vurdere om vi vil understøtte og m/u afspejling?? - Nu udnytter vi det altså
//		return (ProduktYdtpAng[]) DBServer.getInstance().getVbsf().get(this, ProduktYdtpAng.ProduktYdtpAng);
		OQuery qry = new OQuery(ProduktYdtpAngInclAfspejlingerImpl.class);
		qry.add(getId(), "Aftale");
//		qry = DBServer.getInstance().getVbsf().getGldFremtidigeQuery(qry, pDato);
		ProduktYdtpAngInclAfspejlingerImpl[] afspejlinger = (ProduktYdtpAngInclAfspejlingerImpl[])DBServer.getInstance().getVbsf().queryExecute(qry);
		return afspejlinger;
	}

	/**
	 * @param pqry
	 * @return alle ProduktYdtpAng[] der matcher OQuery-argument og excl. afspejlede ydelser
	 */
	private ProduktYdtpAng[] getProduktYdtpAng(OQuery pqry) {
//		decorateQAfspejling(pqry);  // Sker nu i skemaet
		try {
			ProduktYdtpAng[] rels = getProduktYdtpAng2(pqry);
			return rels;
		} catch (IllegalStateException e) {
			DBServer.getInstance().getVbsf().markCollectionDirty(this, ProduktYdtpAng.ProduktYdtpAng);
//			Debug.setDebugging(Debugger.DATABASE);
			ProduktYdtpAng[] rels = getProduktYdtpAng2(pqry);
//			Debug.setDebuggingOff();
//			log_.error("Reloadede uden afspejlinger");
			return rels;
		}
	}
	/**
	 * @param pqry
	 * @return alle ProduktYdtpAng[] der matcher OQuery-argument og excl. afspejlede ydelser
	 */
	private ProduktYdtpAng[] getProduktYdtpAng2(OQuery pqry) {
		ProduktYdtpAng[] rels = (ProduktYdtpAng[]) DBServer.getInstance().getVbsf().get(this, ProduktYdtpAng.ProduktYdtpAng, pqry);
		// TOxDO PK Fjern al denne kontrol når det virker...  2017 - vi lader den være...
		if (rels !=null) {
			for (ProduktYdtpAng rel : rels) {
				Produkttype produkttype = rel.getProdukt().getProdukttype();
				Ydelsestype ydelsestype = rel.getYdelsestype();
				PdtpYdtp[] pdtpYdtp = produkttype.getPdtpYdtp();
				if (pdtpYdtp != null) {
					for (PdtpYdtp pdtpYdtp2 : pdtpYdtp) {
						if (pdtpYdtp2.isSpejlet() && pdtpYdtp2.getYdelsestype().equals(ydelsestype)) {
							throw new IllegalStateException("Pdydtang collection indeholder afspejlede ydelsestyper ??? " + 
									produkttype.getBenaevnelse() + " " + ydelsestype.getBenaevnelse());
						}
					}
				}
				if (rel.isAnnulleret() || rel.isOphoert(((FilterQuery)pqry).getDatoFilter()))
					throw new IllegalStateException("Pdydtang collection indeholder ophørte ydelsestyper " + rel.getGldOphLabel());
			}
		}
		return rels;
	}
	public void addProduktYdtpAng(ProduktYdtpAngImpl pProduktYdtpAng) {
		PersistensService.addToCollection(this, ProduktYdtpAng.ProduktYdtpAng, pProduktYdtpAng);
	}

	public void removeProduktYdtpAng(ProduktYdtpAngImpl oldProduktYdtpAng) {
		PersistensService.removeFromCollection(this, ProduktYdtpAng.ProduktYdtpAng, oldProduktYdtpAng);
	}

	public Object[] getAftaleBetalingsfrist() {
		return DBServer.getInstance().getVbsf().get(this, "AftaleBetalingsfrist");
	}

	public void addAftaleBetalingsfrist(AftaleBetalingsfrist pAftaleBetalingsfrist) {
		PersistensService.addToCollection(this, "AftaleBetalingsfrist", pAftaleBetalingsfrist);
	}

	public void removeAftaleBetalingsfrist(AftaleBetalingsfrist oldAftaleBetalingsfrist) {
		PersistensService.removeFromCollection(this, "AftaleBetalingsfrist", oldAftaleBetalingsfrist);
	}

	public BigDecimal getBetalingsfrist(BigDecimal pGaeldendeDato) {
		Object[] aftaleBetalingsfrist = DBServer.getInstance().getVbsf().get(this, "AftaleBetalingsfrist", pGaeldendeDato);
		AftaleArtp aftaleArtp = getAftaleArtp(pGaeldendeDato);
		if ((aftaleBetalingsfrist != null) && (aftaleBetalingsfrist.length > 0))
			for (int i = 0; i < aftaleBetalingsfrist.length; i++)
				if ((((AftaleBetalingsfrist) aftaleBetalingsfrist[i]).getGldArtp().equals(aftaleArtp.getGld()))
						&& (((AftaleBetalingsfrist) aftaleBetalingsfrist[i]).getAfregningstype().equals(aftaleArtp.getAfregningstype())))
					return ((AftaleBetalingsfrist) aftaleBetalingsfrist[i]).getAntalefristdageefterforfaldsdato();

		return null;
	}

	/**
	 * @return både hovedprodukter og dækninger, uanset om gældende eller ej
	 */
	public Produkt[] getProdukt() {
		return (Produkt[]) DBServer.getInstance().getVbsf().get(this, Produkt.PRODUKT);
	}
	public boolean hasDaekning() {
		// Metoden aflæser fra objectcache hvis muligt.
		final Produkt[] enDaekning = (Produkt[]) DBServer.getInstance().getVbsf().get(this, Produkt.PRODUKT, ProduktImpl.getHasDaekningQuery());
		return enDaekning != null && enDaekning.length > 0;
	}

	/**
	 * @return både hovedprodukter og dækninger pr. datoen
	 */
	public Produkt[] getProduktGld(BigDecimal pGaeldendeDato) {
		return (Produkt[]) DBServer.getInstance().getVbsf().get(this, Produkt.PRODUKT, pGaeldendeDato);
	}

	public void addAftalekompBttp(KlausulholderKlausultype pAftalekompBttp) {
		addBttpAftale((BttpAftale) pAftalekompBttp);
	}

	public void removeAftalekompBttp(KlausulholderKlausultype pAftalekompBttp) {
		removeBttpAftale((BttpAftale) pAftalekompBttp);
	}

	public BttpAftale[] getBttpAftale() {
		return (BttpAftale[]) DBServer.getInstance().getVbsf().get(this, "BttpAftale");
	}

	public BttpAftale[] getBttpAftale(BigDecimal pGaeldendeDato) {
		BttpAftale[] bttpAftale = getBttpAftale();
		if ((bttpAftale != null) && (bttpAftale.length > 0)) {
			BttpAftale[] bttpAftales = (BttpAftale[]) Datobehandling.findGaeldende(bttpAftale, pGaeldendeDato);
			ReflectionSort.sort(bttpAftales, "getSomethingToSort"); // sorter efter klausultypesbenævnelse, 2. parm skal være en getmetode til attributten
			return bttpAftales;
		}
		return null;
	}

	public void addBttpAftale(BttpAftale pBttpAftale) {
		PersistensService.addToCollection(this, "BttpAftale", pBttpAftale);
	}

	public void removeBttpAftale(BttpAftale pOldBttpAftale) {
		PersistensService.removeFromCollection(this, "BttpAftale", pOldBttpAftale);
	}

//	/** Hent klausuler tilknyttet denne aftale */
//	public Betingelsestype[] getBetingelsestype(BigDecimal dato) {
//		return (Betingelsestype[]) DBServer.getInstance().getVbsf().get(this, "BttpAftale", dato);
//	}

	/**
	 * Hent aktive betingelser tilknyttet på denne aftale pr. pDato.
	 *
	 * Forudsætter, at aftaleegenskabsgruppes benævnelse enten starter med "Betingelse" eller med 'Vilkår'
	 */
	public String[] getAftaleEgenskabBetingelser(BigDecimal pDato){
		List<String> betingelser = new ArrayList<>();
		String sql = "SELECT IDENT FROM afeggp WHERE BNV like 'Betingelse%' or BNV like 'Vilkår%'";
		OQuery qry = new OQuery(AftaleegngrpImpl.class);
		qry.add(sql, "aftaleegngrpId", OQuery.IN);
		AftaleegngrpImpl[] aftaleegngrupper = (AftaleegngrpImpl[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		for (int i = 0; aftaleegngrupper != null &&  i < aftaleegngrupper.length; i++) {
			AftaleAfegn aftaleAfegn = getAftaleAfegn(aftaleegngrupper[i], pDato);
			if (aftaleAfegn != null){
				betingelser.add(aftaleAfegn.getEgenskab().getBenaevnelse().trim());
			}
		}
		return ContainerUtil.toArray(betingelser);
	}

	@Override
	public List<String> getAftaleBetingelser(BigDecimal pDato) {
		String[] aftaleEgenskabBetingelser = getAftaleEgenskabBetingelser(pDato);
		if (aftaleEgenskabBetingelser == null) {
			return new ArrayList<>();
		}
		return Arrays.asList(aftaleEgenskabBetingelser);
	}

	public BttpAftale[] getBttpAftaleFremtidige(BigDecimal pDato) {
		BttpAftale[] alleBttp = getBttpAftale();
		if (alleBttp != null) {
			BttpAftale[] bttpAftales = (BttpAftale[]) Datobehandling.findFremtidige(alleBttp, pDato);
			ReflectionSort.sort(bttpAftales, "getSomethingToSort"); // sorter efter klausultypesbenævnelse, 2. parm skal være en getmetode til attributten
			return bttpAftales;
		}
		return null;
	}

	public BttpAftale[] getBttpAftaleGldOgFremtidige(BigDecimal pDato) {
		BttpAftale[] alleBttp = getBttpAftale();
		if (alleBttp != null) {
			BttpAftale[] bttpAftales = (BttpAftale[]) Datobehandling.findGaeldendeOgFremtidige(alleBttp, pDato);
			ReflectionSort.sort(bttpAftales, "getSomethingToSort"); // sorter efter klausultypesbenævnelse, 2. parm skal være en getmetode til attributten
			return bttpAftales;
		}
		return null;
	}

	public void addOpsigelse(Opsigelse pOps) {
		PersistensService.addToCollection(this, "Opsigelse", pOps);
	}

	public void removeOpsigelse(Opsigelse pOps) {
		PersistensService.removeFromCollection(this, "Opsigelse", pOps);
	}/*
		 * private AftaleOpsigelse[] getAftaleOpsigelser(){ return
		 * (AftaleOpsigelse[])DBFactory.getCollection(this,"AftaleOpsigelse"); }
		 */

	public void addAftalekompRabattype(AftalekompRabattypeIF pAftalekompRabattype) {
		addAftaleRabattype((AftaleRbtp) pAftalekompRabattype);
	}

	public void removeAftalekompRabattype(AftalekompRabattypeIF pAftalekompRabattype) {
		removeAftaleRabattype((AftaleRbtp) pAftalekompRabattype);
	}

	public BigDecimal getRabatIalt(BigDecimal gld) {
		BigDecimal svar = BigDecimal.ZERO;
		AftaleRbtpPdIF[] afrb = findAftaleRbtpPdIF(gld);
		if (afrb != null) {
			for (AftaleRbtpPdIF rel : afrb) {
				svar = svar.add(rel.getRabatbeloeb());
			}
		}

		Produkt[] daekningerGld = getDaekningerGld(gld);
		for (Produkt produkt : daekningerGld) {
			ProduktRbtpIF[] rels = produkt.findProduktRbtpIF(gld);
			if (rels != null) {
				for (ProduktRbtpIF rel : rels) {
					svar = svar.add(rel.getRabatbeloeb());
				}
			}
		}
		return svar;
	}
	
	public AftaleRbtp[] getAftaleRabatTyper() {
		return (AftaleRbtp[]) DBServer.getInstance().getVbsf().get(this, "AftaleRbtp");
	}

	public AftaleRbtp[] getAftaleRabatTyper(BigDecimal pDato) {
		return (AftaleRbtp[]) DBServer.getInstance().getVbsf().get(this, "AftaleRbtp", pDato);
	}

    public AftaleRbtp getAftaleRabatType(Rabattype rabattype, BigDecimal gld) {
        AftaleRbtp[] aftaleRabatTyper = getAftaleRabatTyper(gld);
        if (aftaleRabatTyper != null){
            for (AftaleRbtp rel : aftaleRabatTyper) {
                if (rel.getRabattype().equals(rabattype))
                    return rel;
            }
        }
        return null;
    }

	public AftaleRbtp[] getAftaleRabatTyperGldOgFremtidige(BigDecimal pDato) {
		return (AftaleRbtp[]) Datobehandling.findGaeldendeOgFremtidige(getAftaleRabatTyper(), pDato);
	}

	/*
	 * public void setAftaleRabatTyper(AftaleRbtp[] pAftaleRabatTyper){
	 * DBFactory.setReference(this,"AftaleRbtp",pAftaleRabatTyper); }
	 */
	public void addAftaleRabattype(AftaleRbtp pAftaleRabattype) {
		PersistensService.addToCollection(this, "AftaleRbtp", pAftaleRabattype);
	}

	public void removeAftaleRabattype(AftaleRbtp pAftaleRabattype) {
		PersistensService.removeFromCollection(this, "AftaleRbtp", pAftaleRabattype);
	}

	// 26.02.01 - Implementeret
	public Produkt[] getDaekningerGld(BigDecimal pGaeldendeDato) {
		return getGrundProduktGld(pGaeldendeDato);
//		return (Produkt[]) ContainerUtil.arraycopy(getGrundProduktGld(pGaeldendeDato), Produkt.class);
	}
	public List<Produkt> getDaekningerGldFremtidige(BigDecimal pDato) {
		OQuery qry = new OQuery();
		qry = DBServer.getInstance().getVbsf().getGldFremtidigeQuery(qry, pDato);
		qry.add("       ", "Emne", OQuery.NOT_EQUAL, OQuery.AND);
		Produkt[] p = (Produkt[]) DBServer.getInstance().getVbsf().get(this, Produkt.PRODUKT, qry);
		return ContainerUtil.asList(p);
	}
	public Produkt[] getDaekninger() {
		Produkt[] daekninger_ = null;
		if (true) {
			daekninger_ = getGrundProdukt();
			// (Produkt[])ContainerUtil.arraycopy(getGrundProdukt(),Produkt.class);
			// daekningerLoaded_ = true;

			// har vi fået fat i nogle af de få aftaler som er sluppet ud af CUA uden at få genveje på ?
			// Når den mulighed ikke længere er til stede, kan denne for-løkke fjernes
			for (int i = 0; daekninger_ != null && i < daekninger_.length; i++) {
				if (((ProduktImpl) daekninger_[i]).Emne.trim().equals("")) {
					if (GensamUtil.isRunningOnline()) {
						JOptionPane.showMessageDialog(null, getGenvejsfejlTekst(), "Datafejl", JOptionPane.ERROR_MESSAGE, null);
					} else {
						log_.error("9800 !!!! Aftale " + getId() + " har ingen genveje ");
					}
					break;
				}
			}

		}
		// log_.info("++++++ af " + getId() + " hash " + this.hashCode() + " antal dækninger " +
		// (daekninger_ == null ? 0 : daekninger_.length) +
		// "[T " + Thread.currentThread() + "]");
		return daekninger_;
	}

	/**
	 * Hjælpemetode som skal fjernes når ovenstående validering fjernes
	 */
	private String getGenvejsfejlTekst() {
		return "Policenummer " + getXOAftaleLabelTxt(Datobehandling.getDagsdatoBigD())
				+ " kan ikke vises korrekt på skærmen på grund af \nuoverensstemmelser i de gemte oplysninger. "
				+ "\n\nFor at rette op på de gemte oplysninger skal du\n" + "    1.  skifte til Gaia/cua\n"
				+ "    2.  indtaste valg \"12 = Arbejd med\" ud for f.eks. forsikringen på skærmbilledet Aftaler   \n"
				+ "    3.  trykke F12 for at forlade forsikringen\n\n" + "Når du returnerer til Gensafe Pro kan du arbejde videre med forsikringen\n"
				+ "efter tryk på \"Opfrisk\" over fanebladene.\n\n";
	}

	public Produkt[] getDaekninger(Genstand pGenstand) {
		Produkt[] alleDaekninger = ContainerUtil.arraycopy(getDaekninger(), Produkt.class);
		ArrayList<Produkt> daekninger = new ArrayList<Produkt>();
		for (int i = 0; alleDaekninger != null && i < alleDaekninger.length; i++) {
			if (alleDaekninger[i].getGenstandId() != null && alleDaekninger[i].getGenstandId().equals(pGenstand.getId()))
				daekninger.add(alleDaekninger[i]);
		}
		return ContainerUtil.toArray(daekninger);
	}

	public Produkt[] getDaekninger(Emne pEmne) {
		Produkt[] alleDaekninger = ContainerUtil.arraycopy(getDaekninger(), Produkt.class);
		ArrayList<Produkt> daekninger = new ArrayList<Produkt>();
		for (int i = 0; alleDaekninger != null && i < alleDaekninger.length; i++) {
			if (alleDaekninger[i].getEmneId().equals(pEmne.getId()))
				daekninger.add(alleDaekninger[i]);
		}
		return ContainerUtil.toArray(daekninger);
	}

	public Hovedprodukt[] getHovedproduktGld(BigDecimal pGaeldendeDato) {
		// Object[] produkter = ContainerUtil.foreningsmaengde(DBFactory.get(this, "Hovedprodukt", pGaeldendeDato),
		// DBFactory.getAdded(this, "Hovedprodukt", pGaeldendeDato));
		Hovedprodukt[] produkter = (Hovedprodukt[]) DBServer.getInstance().getVbsf().get(this, "Hovedprodukt", pGaeldendeDato);
		return filtrerProdukter(produkter);
	}

	public AftaleYdtpAng[] getAftaleYdtpAngGld(BigDecimal pGaeldendeDato, QueryService.Datefilters pDateFilter) {
		if (pGaeldendeDato != null) {
			if (pDateFilter.equals(QueryService.Datefilters.GLD))
				return (AftaleYdtpAng[])DBServer.getInstance().getVbsf().get(this, AftaleYdtpAng.AftaleYdtpAng, pGaeldendeDato);
			
			if (pDateFilter.equals(QueryService.Datefilters.GLD_FREMTIDIG))
				return (AftaleYdtpAng[])DBServer.getInstance().getVbsf().get(this, pGaeldendeDato, AftaleYdtpAng.AftaleYdtpAng);

			throw new UnsupportedOperationException("Understøtter kun GLD og GLD_FREMTIDIG p.t.");
		}
		return (AftaleYdtpAng[])DBServer.getInstance().getVbsf().get(this, AftaleYdtpAng.AftaleYdtpAng);
	}

	protected Hovedprodukt[] filtrerProdukter(Hovedprodukt[] pProdukter) {
		Vector<Hovedprodukt> vReturprodukter = new Vector<>(1);
		// Asgnm23017 - der bør kun findes eet af slagsen
		// De gamle Nord 280'ere har to, men det ene er annulleret
		// så det prøver vi at undgå
		Hovedprodukt annulleretHovedProdukt = null;
		if (pProdukter != null) {
			for (int i = 0; i < pProdukter.length; i++) {
				if (!pProdukter[i].getProdukttype().isGrundprodukt()) {
					if (pProdukter[i].isAnnulleret()) {
	                    annulleretHovedProdukt = pProdukter[i];
                    }
					else {
						vReturprodukter.addElement(pProdukter[i]);
					}
				}
			}
			if (vReturprodukter.size() == 0) {
				// Nå, det var så åbenbart det eneste der var, så det skal nok bruges alligevel
				if (annulleretHovedProdukt == null) {
					return null;
                }
				else {
	                vReturprodukter.addElement(annulleretHovedProdukt);
				}
			}
			Hovedprodukt[] returProdukter = new Hovedprodukt[vReturprodukter.size()];
			vReturprodukter.copyInto(returProdukter);
			return returProdukter;
		}
		return null;
	}

	public Hovedprodukt[] getHovedprodukt() {
		// Object[] produkter = ContainerUtil.foreningsmaengde(DBFactory.get(this, "Hovedprodukt"),
		// DBFactory.getAdded(this, "Hovedprodukt"));
		Hovedprodukt[] produkter = (Hovedprodukt[]) DBServer.getInstance().getVbsf().get(this, "Hovedprodukt");
		return filtrerProdukter(produkter);
	}

	public void addProdukt(Produkt pProdukt) {
		PersistensService.addToCollection(this, Produkt.PRODUKT, pProdukt);
	}

	public void addHovedprodukt(HovedproduktImpl pHovedprodukt) {
		PersistensService.addToCollection(this, "Hovedprodukt", pHovedprodukt);
		ProduktImpl produkt = DBServer.getInstance().getVbsf().lookup(ProduktImpl.class, pHovedprodukt.getId());

		PersistensService.addToCollection(this, Produkt.PRODUKT, produkt);
	}

	public void removeProdukt(Produkt oldProdukt) {
		PersistensService.removeFromCollection(this, Produkt.PRODUKT, oldProdukt);
	}

	private XOAftaleLabelImpl[] getXOAftaleLabel(BigDecimal pGaeldendeDato) {
		return (XOAftaleLabelImpl[]) DBServer.getInstance().getVbsf().get(this, XOAftaleLabelImpl.XOAftaleLabelColl, pGaeldendeDato);
	}

	public Object[] refreshXOAftaleLabel() {
		DBServer.getInstance().getVbsf().markCollectionDirty(this, XOAftaleLabelImpl.XOAftaleLabelColl);
		return null;
	}
//
//	public void addXOAftaleLabel(XOAftaleLabelImpl pXOAftaleLabel) {
////		PersistensService.addToCollection(this, XOAftaleLabel.XOAftaleLabelColl, pXOAftaleLabel);
//	}
//
//	public void removeXOAftaleLabel(XOAftaleLabelImpl oldXOAftaleLabel) {
////		PersistensService.removeFromCollection(this, XOAftaleLabel.XOAftaleLabelColl, oldXOAftaleLabel);
//	}

	public Object[] getAftaleYdtp() {
		return DBServer.getInstance().getVbsf().get(this, "AftaleYdtp");
	}

	public void addAftaleYdtp(AftaleYdtp pAftaleYdtp) {
		PersistensService.addToCollection(this, "AftaleYdtp", pAftaleYdtp);
	}

	public void removeAftaleYdtp(AftaleYdtp oldAftaleYdtp) {
		PersistensService.removeFromCollection(this, "AftaleYdtp", oldAftaleYdtp);
	}

	public Object[] getAfYdtpYdVartp() {
		return DBServer.getInstance().getVbsf().get(this, "AfYdtpYdVartp");
	}

	public void addAfYdtpYdVartp(AfYdtpYdVartp pAfYdtpYdVartp) {
		PersistensService.addToCollection(this, "AfYdtpYdVartp", pAfYdtpYdVartp);
	}

	public void removeAfYdtpYdVartp(AfYdtpYdVartp oldAfYdtpYdVartp) {
		PersistensService.removeFromCollection(this, "AfYdtpYdVartp", oldAfYdtpYdVartp);
	}

	public AftalekompYdtpYdVartpIF[] getAftalekompYdtpYdVartp() {
		return ContainerUtil.arraycopy(getAfYdtpYdVartp(), AftalekompYdtpYdVartpIF.class);
	}

	public void addAftalekompYdtpYdVartp(AftalekompYdtpYdVartpIF pAftalekompYdtpYdVartp) {
		addAfYdtpYdVartp((AfYdtpYdVartp) pAftalekompYdtpYdVartp);
	}

	public void removeAftalekompYdtpYdVartp(AftalekompYdtpYdVartpIF pAftalekompYdtpYdVartp) {
		removeAfYdtpYdVartp((AfYdtpYdVartp) pAftalekompYdtpYdVartp);
	}

	@Override
	public Aftale getAftalen() {
		return this;
	}

	@Override
	public Genstand getGenstanden() {
		return null;
	}

	/*
	 * public Object[] getAftaleAfegn() throws BODBException { //return
	 * ContainerUtil.foreningsmaengde(DBFactory.get(this,"AftaleAfegn"), DBFactory.getAdded(this,"AftaleAfegn")); return
	 * (DBFactory.list(this,"AftaleAfegn")); }
	 */

	// Tilføjelser - start
	public static String getTabelnavn() {
		return tabelnavn;
	}

	public AftaletypeImpl getAftaleTypen() {
		// if (typeId != null && !typeId.trim().equals("")) {
		// if (aftpo_ == null)
		return DBServer.getInstance().getVbsf().lookup(AftaletypeImpl.class, typeId);
		// return aftpo_;
		// }
		// // bør ikke komme her. Hvis det sker kastes unsupportedexc i getmetoden
		// Object [] o = this.getAftalesAftp();
		// return ((AftalesAftp)o[0]).getAftaletype(); /* altid kun 1 forekomst */
	}

	public Produkt[] getGrundProdukt() {
		Produkt[] produkter = getProdukt();
		// Produkt[] grundProdukter;
		Vector<Produkt> vGrundProdukter = new Vector<Produkt>();
		if (produkter != null) {
			for (int i = 0; i < produkter.length; i++) {
				if (((ProduktImpl) produkter[i]).getProdukttype().isGrundprodukt()) {
					vGrundProdukter.addElement(produkter[i]);
				}
			}
		}
		return ContainerUtil.toArray(vGrundProdukter);
		// grundProdukter = new Object[vGrundProdukter.size()];
		// vGrundProdukter.copyInto(grundProdukter);
		// return grundProdukter;
	}

	// /**
	// wrapper metode til getGrundProdukt() som returnerer collection med korrekt type
	// */
	// public Produkt[] getGrundProduktRetur() {
	// Object[] o = this.getGrundProdukt();
	// if (o == null) return null;
	// Produkt[] d = new Produkt[o.length];
	//
	// for (int i = 0; i<o.length; i++) {
	// d[i] = (Produkt)o[i];
	// }
	// return d;
	// }

	public Produkt[] getGrundProduktGld(BigDecimal pGaeldendeDato) {
		Produkt[] produkter = getGrundProdukt();
		if ((produkter != null) && (produkter.length > 0)) {
			return (Produkt[]) Datobehandling.findGaeldende(produkter, pGaeldendeDato);
		}
		return null;
	}

	// Bemærk, at den er mere favnende end super.equals. Her skal begge objekter blot være af samme type - ikke samme klasse.
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Aftale) {
			return this.getId().equals(((Aftale) obj).getId());
		}
		return false;
	}

	/**
	 * Returnerer den gældende aftalelabel pr. den givne dato. Metoden sørger selv for at korrigere datoen til at ligge
	 * indenfor gyldighedsperioden.
	 * 
	 * @param pGaeldendeDato
	 * @return Label, typisk et policenr.
	 */
	public String getXOAftaleLabelTxt(BigDecimal pGaeldendeDato) {
//		XOAftaleLabelImpl[] xoAftaleLabelTabel = getXOAftaleLabelGld(getOpgDatoKorrigeret(pGaeldendeDato));
		XOAftaleLabelImpl[] xoAftaleLabelTabel = getXOAftaleLabel(getOpgDatoKorrigeret(pGaeldendeDato));
		if ((xoAftaleLabelTabel != null) && (xoAftaleLabelTabel.length > 0)) {
			return xoAftaleLabelTabel[0].getAftalelabel();
		}
		return getAftaletypeBenaevnelse();
	}

	public String getId() {
		return this.getAftaleId();
	}

	public void setId(String pId) {
		this.setAftaleId(pId);
	}

	public String getAftaletypeBenaevnelse() {
		AftaletypeImpl aftaletype = getAftaleTypen();
		if (aftaletype != null) {
			return aftaletype.getBenaevnelse();
		}
		return "";
	}

	/**
	 * Get metode til aftaletypens korte benævnelse
	 */
	public String getAftaletypeKortBenaevnelse() {
		AftaletypeImpl aftaletype = getAftaleTypen();
		if (aftaletype != null) {
			return aftaletype.getKortBenaevnelse();
		}
		return null;
	}

	/**
	 * BEMÆRK: kaldes med reflection
	 * @return aftaletypens sorteringssekvens + label pr. d.d. korrigeret
	 */
	public String getSortSekvens() {
		String r = "";
		AftaletypeImpl aftaletype = getAftaleTypen();
		if (aftaletype != null) {
			r = aftaletype.getSortSekvens();
		}
		return r + this.getLabel();
	}

	/**
	 * Returnere aftalens oprettelsesdato + oprettelsestid til brug til sortering af aftaler efter oprettelsestidspunkt.
	 */
	public String getSortKriterieOprettet() {
		return this.getOprDato().toString() + Datobehandling.redigerTid(getOprTid());
	}

	/**
	 * Returnere alle de fremtidige rabatter på aftalen, i forhold til medsendte dato.
	 */
	public AftaleRbtp[] getFremtidigeRabatter(BigDecimal pDato) {
		AftaleRbtp[] alleRabatter = getAftaleRabatTyper();

		if (alleRabatter != null && pDato != null) {
			AftaleRbtp[] fremtidigeRabatter = (AftaleRbtp[]) Datobehandling.findFremtidige(alleRabatter, pDato);
			return fremtidigeRabatter;

		}
		return null;
	}

	public AftalekompYdtpAngivelseIF[] getYdelsesAngivelser(BigDecimal pGaeldendeDato) {
		AftalekompYdtpAngivelseIF[] tmpIF = getAftaleYdtpAngGld(pGaeldendeDato, QueryService.Datefilters.GLD);
		if (isAfspejlningMulig()) {
			tmpIF = AftalekompYdtpAngivelseImpl.getIkkeAfspejledeAftalekompYdtpAngivelse(tmpIF);
		}
//		tmpIF = AftalekomponentImpl.getYdelsesAngivelserFiltreretPaaXxYdtp(tmpIF, (AftalekompYdtpIF[]) ContainerUtil.arraycopy(getAftaleYdtp(),
//				AftalekompYdtpIF.class), pGaeldendeDato);
		return tmpIF;

		/*
		 * Object[] tmp = getAftaleYdtpAngGld(pGaeldendeDato); if ((tmp != null) && (tmp.length > 0)) { if (tmp[0]
		 * instanceof AftalekompYdtpAngivelseIF) { log_.info("AftaleImpl.getYdelsesAngivelder - Dette er en
		 * AftalekompYdtpAngivelseIF"); } AftalekompYdtpAngivelseIF[] result = new
		 * AftalekompYdtpAngivelseIF[tmp.length]; ContainerUtil.arraycopy(result,tmp); return result; } return null;
		 */
	}

	public AftalekompYdtpAngivelseIF[] getYdelsesAngivelser() {
		AftalekompYdtpAngivelseIF[] tmpIF = getAftaleYdtpAngGld(null, null);
		if (isAfspejlningMulig()) {
			tmpIF = AftalekompYdtpAngivelseImpl.getIkkeAfspejledeAftalekompYdtpAngivelse(tmpIF);
		}
		return tmpIF;
	}

	public AftalekompYdtpAngivelseIF[] getYdelsesAngivelserGldOgFremtidige(BigDecimal pDato) {
		AftalekompYdtpAngivelseIF[] tmpIF = getAftaleYdtpAngGld(pDato, QueryService.Datefilters.GLD_FREMTIDIG);
//			(AftalekompYdtpAngivelseIF[]) Datobehandling.findGaeldendeOgFremtidige(getYdelsesAngivelser(), pDato);
//		tmpIF = AftalekomponentImpl.getYdelsesAngivelserFiltreretPaaXxYdtp(tmpIF, (AftalekompYdtpIF[]) ContainerUtil.arraycopy(getAftaleYdtp(),
//				AftalekompYdtpIF.class), pDato);
		return tmpIF;
	}

	public void addAftalekompYdtpAngivelse(AftalekompYdtpAngivelseIF pAftalekompYdtpAngivelse) {
		addAftaleYdtpAng((AftaleYdtpAngImpl) pAftalekompYdtpAngivelse);
	}

	public void removeAftalekompYdtpAngivelse(AftalekompYdtpAngivelseIF pAftalekompYdtpAngivelse) {
		removeAftaleYdtpAng((AftaleYdtpAngImpl) pAftalekompYdtpAngivelse);
	}

	public AftalekompYdtpAngivelseIF[] getFremtidigeYdelsesAngivelser(Ydelsestype pYdelsestype, BigDecimal pDato) {
		// return AftalekomponentImpl.getFremtidigeYdelsesAngivelser(this, AftaleYdtpAngImpl.class, "Aftale",
		// pYdelsestype, pDato);
		ArrayList<AftalekompYdtpAngivelseIF> angivelser = new ArrayList<AftalekompYdtpAngivelseIF>();
		AftalekompYdtpAngivelseIF[] fremtidigeAngivelser = ContainerUtil.arraycopy(Datobehandling.findFremtidige(
				getAftaleYdtpAngGld(pDato, QueryService.Datefilters.GLD_FREMTIDIG), pDato), AftalekompYdtpAngivelseIF.class);
		for (int i = 0; fremtidigeAngivelser != null && i < fremtidigeAngivelser.length; i++) {
			if (fremtidigeAngivelser[i].getYdelsestype().equals(pYdelsestype))
				angivelser.add(fremtidigeAngivelser[i]);
		}
		return ContainerUtil.toArray(angivelser);

	}

	public AftalekompYdtpIF getAftalekompYdtp(Ydelsestype pYdelsestype) {
		Object[] afYdtp = getAftaleYdtp();
		if ((afYdtp != null) && (afYdtp.length > 0)) {
			for (int i = 0; i < afYdtp.length; i++) {
				if (((AftalekompYdtpIF) afYdtp[i]).getYdelsestype().equals(pYdelsestype))
					return (AftalekompYdtpIF) afYdtp[i];
			}
		}
		return null;
	}

	public void addAftalekompYdtp(AftalekompYdtpIF pAftalekompYdtp) {
		addAftaleYdtp((AftaleYdtp) pAftalekompYdtp);
	}

	public void removeAftalekompYdtp(AftalekompYdtpIF pAftalekompYdtp) {
		removeAftaleYdtp((AftaleYdtp) pAftalekompYdtp);
	}

	public AftalekomponentIF[] getAftaleChildren(BigDecimal pGaeldendeDato) {
		return ContainerUtil.arraycopy(getEmneGld(pGaeldendeDato), AftalekomponentIF.class);
	}

	public AftalekomponentIF[] getAftaleChildrenGaeldendeOgFremtidige(BigDecimal pGaeldendeDato) {
		return ContainerUtil.arraycopy(Datobehandling.findGaeldendeOgFremtidige(getEmne(), pGaeldendeDato),
				AftalekomponentIF.class);
	}

	public AftalekomponentIF getAftaleParent(BigDecimal pGaeldendeDato) {
		return null;
	}

	/**
	 * Get XOLabel pr. datoen. Samme som getXOAftaleLabelTxt(pGaeldendeDato)
	 */
	public String getLabel(BigDecimal pGaeldendeDato) {
		return getXOAftaleLabelTxt(pGaeldendeDato);
	}

	public String getLabelExtended() {
		return getLabelLinierConcateneret() + " " + getAftaleTypen();
	}

	public void accept(AftaleVisitorIF pAftaleVisitor) {
		pAftaleVisitor.visitAftale(this);
		AftalekomponentIF[] children = getAftaleChildren(pAftaleVisitor.getAftaleDato());
		if ((children != null) && (children.length > 0)) {
			for (int i = 0; i < children.length; i++) {
				children[i].accept(pAftaleVisitor);
			}
		}
	}

	public AftalekompTypeIF getAftalekomponentType() {
		return getAftaleTypen();
	}

	public boolean isAfspejlningMulig() {
		return false;
	}

	/*
	 * public GenstandImpl[] getFoersteGenstandGld(BigDecimal pGaeldendeDato) { HovedproduktImpl[] hovedproduktTabel =
	 * this.getHovedproduktGld(); if ((hovedproduktTabel != null) && (hovedproduktTabel.length > 0)) { } }
	 */

//	/**
//	 * Returnere alle aftalens adresser, inkl. pbs adresser
//	 * 
//	 * @return AftaleAdresse[] alle aftalens adresser
//	 */
//	private AftaleAdresse[] getAlleAftaleAdresser() {
//		Object[] temp = DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);
//		AftaleAdresse[] result = (AftaleAdresse[]) ContainerUtil.arraycopy(temp, AftaleAdresse.class);
//		return result;
//	}

//	/**
//	 * Returnere alle tilknyttede PBS adresser på aftalen.
//	 */
//	public AftaleAdresse[] getAftalePBSAdresseAlle() {
//
//		AftaleAdresse[] afPBSad = (AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);
//		Vector<AftaleAdresse> v = new Vector<AftaleAdresse>();
//		if ((afPBSad != null) && (afPBSad.length > 0)) {
//			for (int i = 0; i < afPBSad.length; i++) {
//				if (afPBSad[i].getAdresse().isPbsAdresse()) // Sorter ikke-pbs adresser fra
//					v.add(afPBSad[i]);
//			}
//		}
//
//		AftaleAdresse[] pbsAdresse = new AftaleAdresse[v.size()];
//		v.copyInto(pbsAdresse);
//		return pbsAdresse;
//	}

//	/**
//	 * Returnere aftalens gældende PBS adresser på en angivet dato
//	 * 
//	 * @return AftaleAdresse[] aftalens gældende PBS adresser
//	 */
//	public AftaleAdresse[] getAftalePBSAdresseGld(BigDecimal pDato) {
//
//		AftaleAdresse[] afPBSad = (AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);
//		Vector<AftaleAdresse> v = new Vector<AftaleAdresse>();
//		if ((afPBSad != null) && (afPBSad.length > 0)) {
//			for (int i = 0; i < afPBSad.length; i++) {
//				if (afPBSad[i].getAdresse().isPbsAdresse()) // Sorter ikke-pbs adresser fra
//					v.add(afPBSad[i]);
//			}
//		}
//
//		AftaleAdresse[] pbsAdresse = new AftaleAdresse[v.size()];
//		v.copyInto(pbsAdresse);
//
//		if (pbsAdresse != null && pbsAdresse.length > 0) {
//			Object[] temp = Datobehandling.findGaeldende(pbsAdresse, pDato);
//			AftaleAdresse[] result = ContainerUtil.arraycopy(temp, AftaleAdresse.class);
//			return result;
//		}
//		return null;
//	}
//
//	/**
//	 * 
//	 * @return AftaleAdresse[] alle aftalens adresser uden PBS adresser incl. ophørte
//	 */
//	public AftaleAdresse[] getAftaleAdresse() {
//
//		AftaleAdresse[] afad = (AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);
//		Vector v = new Vector();
//		if ((afad != null) && (afad.length > 0)) {
//			for (int i = 0; i < afad.length; i++) {
//				if (!afad[i].getAdresse().isPbsAdresse()) // Sorter pbs adresser fra
//					v.add(afad[i]);
//			}
//		}
//		AftaleAdresse[] result = new AftaleAdresse[v.size()];
//		v.copyInto(result);
//		return result;
//	}

	public AdresseHolderAdresseIF[] getAdresseTilknytninger(BigDecimal pDato) {
		return getAftaleAdresseGldOgFremtidige(pDato);
	}
	/**
	 * <p>
	 * Finder forsikringens evt. matrikelnr via dataudsøgningen. Hvis flere forsikringssteder returneres det første og
	 * bedste matrikelnummer
	 * 
	 * @param den
	 *            dato matrikelnr skal aflæses
	 * @return forsikringens matrikelnr. eller tom string hvis ingen, kan aldrig returnere null
	 */
	public String getMatrikelnr(BigDecimal pDato) {
		if (pDato == null || pDato.intValue() == 0) {
			throw new IllegalArgumentException("getMatrikelnr kræver udfyldt opsigelsesdato");
		}
		IndividBO individBO = new IndividBO(getTegnesAfIndivid(), null, pDato);
		AftaleBO aftaleBO = new AftaleBO(this, individBO, pDato);
		List<AdresseBO> adbo = aftaleBO.getAdresser();
		AdresseBO selekteretAdresse = (AdresseBO) AftaleBO.getSelected(adbo);
		if (selekteretAdresse!=null) {
			Matrikelnummer mnr = new Matrikelnummer(selekteretAdresse);
			String v = mnr.getValue();
			if (v != null) {
				return v;
			} 
		}
		return "";
	}
	
	/**
	 * Finder aftalens gældende og fremtidige adresser pr. given dato UDEN PBS adresser
	 * 
	 * @param pDato gældende og fremtidige pr. datoen, null = alle
	 * 
	 * @return AftaleAdresse[] aftalens gældende og fremtidige adresser uden PBS adresser
	 */
	public AftaleAdresse[] getAftaleAdresseGldOgFremtidige(BigDecimal pDato) {
		AftaleAdresse[] afad = null;
		if (pDato != null)
			afad = (AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, pDato, AftaleAdresse.AftaleAdresseColl);
		else 
			afad = (AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);
		return afad;
	}

//	/**
//	 * 
//	 * @param afad
//	 * @return argumentet garanteret uden pbs-adresser
//	 */
//	private AftaleAdresse[] removePBSadresser(AftaleAdresse[] afad) {
//		if (afad == null || afad.length < 1)
//			return null;
//		List<AftaleAdresse> v = new ArrayList<AftaleAdresse>(afad.length);
//		for (int i = 0; i < afad.length; i++) {
//			if (!afad[i].getAdresse().isPbsAdresse()) // filtrer pbs adresser fra
//				v.add(afad[i]);
//		}
//		return ContainerUtil.toArray(v);
////		AftaleAdresse[] result = new AftaleAdresse[v.size()];
////		return result;
//		
//	}

	/**
	 * @return aftalens gældende adresser excl. PBS-adresse
	 */
	public AftaleAdresse[] getAftaleAdresse(BigDecimal gaeldende) {
		AftaleAdresse[] afads = getAftaleAdresseGldOgFremtidige(gaeldende);
		if (afads != null && afads.length > 0)
			return (AftaleAdresse[]) Datobehandling.findGaeldende(afads, gaeldende);
		return null;
		
//		return (AftaleAdresse[]) DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl, gaeldende);
	}

	/**
	 * Returnere alle fremtidige aftaleadresser
	 */
	public AftaleAdresse[] getFremtidigeAftaleAdresser(BigDecimal pDato) {
		AftaleAdresse[] alleAdresser = getAftaleAdresseGldOgFremtidige(pDato);
		if (alleAdresser != null) {
			return (AftaleAdresse[]) Datobehandling.findFremtidige(alleAdresser, pDato);
		}
		return null;
	}
	/**
	 * 
	 * @return et forsikringssted på aftalen uden ophørsdato, tilfældig valgt ved flere, null hvis ingen
	 */
	public Adresse getForsikringsssted(){
		AftaleAdresse[] afad = getAftaleAdresseGldOgFremtidige(Datobehandling.getDagsdatoBigD());
		for (int i = 0;afad != null && i < afad.length; i++) {
			if (afad[i].getAdresse().getAdressetype().isReasAdressetype()){
				if (!afad[i].isOphUdfyldt()){
					return afad[i].getAdresse();
				}
			}
		}
		return null;
	}

	/**
	 * Finder gældende AftaleAdresse relation, hvor adressen har den angivne type.
	 * 
	 * @param type
	 *            Typen adressen skal have
	 * @return AftaleAdresse pr. DD, for fremtidige pr. forsikringens ikraft
	 */
	public AftaleAdresse getAftaleAdresse(Adressetype type) {
//		if (type.isPbsAdressetype()) {
//			AftaleAdresse[] pbsadr = this.getAftalePBSAdresseGld(nyesteLabelGld);
//			return pbsadr != null && pbsadr.length > 0 ? pbsadr[0] : null;
//		}
		BigDecimal pDato = this.isFremtidig() ? getGld() : Datobehandling.getDagsdatoBigD();
		AftaleAdresse[] galdende = this.getAftaleAdresse(pDato);
//			(AftaleAdresse[])DBServer.getInstance().getVbsf().get(this, AftaleAdresse.AftaleAdresseColl);

		if (galdende == null || galdende.length == 0) {
			return null;
		}

		// Gældende fra dagsdato
//		Object[] galdende = Datobehandling.findGaeldende(temp, pDato);
		AftaleAdresse result = null;
		AftaleAdresse aa = null;
		boolean found = false;

		for (int i = 0; i < galdende.length && !found; i++) {
			aa = galdende[i];
			if (aa.getAdresse().getAdressetype().equals(type)) {
				// første aftaleadresse med rigtig type vælges.
				if (result == null) {
					result = aa;
					// hvis aa er gældende efter result er ophørt vælges den i stedet.
				} else if ((aa.getGld().doubleValue() > result.getOph().doubleValue())) {
					result = aa;
				}
				found = result.getOph().doubleValue() == 0;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param pEgenskabsgruppe, null = alle
	 * @return AftaleAfegn[] eller null hvis ingen
	 */
	public AftaleAfegn[] getAftaleAfegn(Egenskabsgruppe pEgenskabsgruppe) {
		if (pEgenskabsgruppe != null){
			OQuery qry = new OQuery();
			qry.add(pEgenskabsgruppe.getId(), "EgenskabsgruppeId");
			return (AftaleAfegn[])DBServer.getInstance().getVbsf().get(this, "AftaleAfegn", qry);
		}
		return (AftaleAfegn[])DBServer.getInstance().getVbsf().get(this, "AftaleAfegn");
	}

	@Override
	public Egenskab getFelt(Egenskabsgruppe afeggp) {
		return getFelt(afeggp, null);
	}
	
	@Override
	public Egenskab getFelt(Egenskabsgruppe afeggp, BigDecimal pDato) {
		if (afeggp == null) return null;
		EgenskabHolderEgenskab[] afafeg = this.getAftaleAfegn(afeggp);
		for (int i = 0; afafeg != null && i < afafeg.length; i++) {
			if (true) {
				if ((pDato == null && !afafeg[i].isOphUdfyldt())
						|| (pDato != null && afafeg[i].isGld(pDato)))
					return afafeg[i].getEgenskab();
			}
		}
		return null;

	}
	@Override
	public Egenskab getFelt(Gsprofelt kortBnve, BigDecimal pDato) {
		return getFelt(RegelServer.getInstance().getAftalegrp(kortBnve.toString()), pDato);
	}
	@Override
	public Egenskab getFelt(Gsprofelt kortBnve) {
		return getFelt(kortBnve, null);
	}

	/**
	 * @param Ydelsestypen gældende på datoen, null = nyeste - den uden ophørsdato - som kan være fremtidig
	 * @return ydelsesangivelsen, null hvis ingen
	 */
	public BigDecimal getFelt(Ydelsestype pYdtp, BigDecimal pDato) {
		AftalekompYdtpAngivelseIF[] ydelsesang = getAftaleYdtpAngGld(pDato, QueryService.Datefilters.GLD);
		
		for (int i = 0; ydelsesang != null && i < ydelsesang.length; i++) {
			if (ydelsesang[i].getYdelsestype().equals(pYdtp)) {
				if ((pDato == null && !ydelsesang[i].isOphUdfyldt())
						|| (pDato != null && ydelsesang[i].isGld(pDato)))
					return ydelsesang[i].getBelobsangivelse();
			}
		}
		return null;

	}
	/**
	 * Returnerer den relation med den angivne egenskabsgruppe, som er gældende pr. den angivne dato.
	 * 
	 * @return AftaleAfegn eller null hvis null-arg eller ingen
	 */
	public AftaleAfegn getAftaleAfegn(Aftaleegngrp afeggp, BigDecimal pDato) {
		if (afeggp == null)
			return null;
		EgenskabHolderEgenskab[] afafeg = this.getAftaleAfegn(afeggp);
		if (afafeg != null) {
			afafeg = (AftaleAfegn[]) Datobehandling.findGaeldende(afafeg, pDato);
		}
		if (afafeg != null && afafeg.length > 0)
			return (AftaleAfegn)afafeg[0];
//		for (int i = 0; afafeg != null && i < afafeg.length; i++) {
//			if (afafeg[i].getEgenskabsgruppeId().equals(afeggp.getId()))
//				return afafeg[i];
//		}

		return null;

	}
	
	
	public void addAftaleAfegn(AftaleAfegn pAftaleAfegn) {
		PersistensService.addToCollection(this, "AftaleAfegn", pAftaleAfegn);
	}

	public void removeAftaleAfegn(AftaleAfegn pOldAftaleAfegn) {
		PersistensService.removeFromCollection(this, "AftaleAfegn", pOldAftaleAfegn);
	}

	// egenskabholder interface ->
	public AftaleAfegn[] getEgenskabHolderEgenskab() {
		return getAftaleAfegn(null);
	}

	public EgenskabHolderEgenskab findEgenskabHolderEgenskab(Egenskabsgruppe pEgenskabsgruppe, BigDecimal pAktuelDato) {
		return EgenskabHolderImpl.findEgenskabHolderEgenskab(this, pEgenskabsgruppe, pAktuelDato);
	}

	public EgenskabHolderEgenskab[] findFremtidigeEgenskabHolderEgenskab(Egenskabsgruppe pEgenskabsgruppe, BigDecimal pAktuelDato) {
		return EgenskabHolderImpl.findFremtidigeEgenskabHolderEgenskab(this, pEgenskabsgruppe, pAktuelDato);
	}

	public FriTekst findFriTekst(Egenskab pEgenskab) {
		return EgenskabHolderImpl.findFriTekst(this, pEgenskab, AftaleegenskabImpl.NIVEAU);
	}

	public Type[] getType() {
		return EgenskabHolderImpl.getType(this);
	}

	public EgenskabHolderType[] getEgenskabHolderType() {
		return ContainerUtil.arraycopy(getTypeTilhoersForhold(), EgenskabHolderType.class);
	}

	public EgenskabHolderType getEgenskabHolderType(Type pType) {
		return EgenskabHolderImpl.getEgenskabsholderType(this, pType);
	}

	public boolean isTilknyttetType(Type pType) {
		return EgenskabHolderImpl.isTilknyttetType(this, pType);
	}

	public String[] getLabelLinier() throws DBException {
		return EgenskabHolderImpl.getLabelLinier(this);
	}

	public String getLabelLinierConcateneret() throws DBException {
		return EgenskabHolderImpl.getLabelLinierConcateneret(this);
	}

	public void addEgenskabHolderEgenskab(EgenskabHolderEgenskab pEgenskabHolderEgenskab) {
		addAftaleAfegn((AftaleAfegn) pEgenskabHolderEgenskab);
	}

	public void removeEgenskabHolderEgenskab(EgenskabHolderEgenskab oldEgenskabHolderEgenskab) {
		removeAftaleAfegn((AftaleAfegn) oldEgenskabHolderEgenskab);
	}

	public void addEgenskabHolderType(EgenskabHolderType pEgenskabHolderType) throws BOReferenceNotUpdatedException {
	}

	public void removeEgenskabHolderType(EgenskabHolderType oldEgenskabHolderType) throws BOReferenceNotUpdatedException {
	}

	private Object[] getTypeTilhoersForhold() {
		Object[] o = new Object[1];
		o[0] = new AftalesAftpImpl(aftaleId, typeId);
		return o;
		// return DBFactory.getCollection(this, "AftalesAftp");

	}

	/**
	 * @param pDato gældende og fremtidige
	 * 
	 * @return aftalens gældende og fremtidige genstande pr. den givne dato (som om nødvendigt korrigeres) eller null hvis ingen.
	 * Excl. genstande kun med ophørte dækninger pr. pDato
	 */
	public List<Genstand> getGenstandeGldogFremtidige(BigDecimal pDato) {
		Genstand[] gns = (Genstand[]) DBServer.getInstance().getVbsf().get(this, getOpgDatoKorrigeret(pDato), "Genstand");
		if (gns != null && gns.length > 0) {
			// Nu har vi så genstande som er gældende eller fremtidige i forhold til genstandens egen gld og oph
			// Nu fjerner vi genstande kun med ophørte dækninger på pDato
			List<Genstand> retur = new ArrayList<Genstand>(gns.length);
			for (Genstand genstand : gns) {
				Produkt[] d = genstand.getDaekninger();
				boolean harD = false;
				for (int i = 0;d != null && i < d.length; i++) {
					if (!d[i].isOphoert(pDato) && !d[i].isAnnulleret()){
						harD = true;
						break;
					}
				}
				if (!genstand.isOphoert(pDato) && !genstand.isAnnulleret() && harD)
					retur.add(genstand);
			}
			return retur.isEmpty() ? null : retur;
		}
		return null;
	}

	/**
	 * Samme som {@link getGenstande(pDato)}
	 */	
	public ArrayList<Genstand> getGenstandeGld(BigDecimal pDato) {
		return (ArrayList<Genstand>) this.getGenstande(pDato);
	}

	@Override
	public List<GenstandObjectIF> getGenstandsObjekterAlle() {
		List<Genstand> lstGenstande = null;
		List<Emne> lstEmner = null;
		lstGenstande = ContainerUtil.asList(getGenstand());
		lstEmner = ContainerUtil.asList(getEmne());
		return getGenstandsObjekter(lstGenstande, lstEmner, true);
//		return (ArrayList) Datobehandling.findGaeldende(getGenstandsObjekter(), pDato);
	}
	/**
	 * 
	 * @param genstande - liste med alle ønskede genstande
	 * @param emner - liste med ønskede emner
	 * @param pInclAnnullerede true incl. annullerede, false excl. annullerede
	 * 
	 * @return første argument suppleret med de emner fra andet argument som ikke selv har genstande tilknyttet excl. annullerede objekter
	 */
	private List<GenstandObjectIF> getGenstandsObjekter(List<Genstand> genstande, List<Emne> emner, boolean pInclAnnullerede) {

		if (genstande == null)
			genstande = new ArrayList<Genstand>(0);
		if (emner == null)
			emner = new ArrayList<Emne>(0);
		
		for (int i = 0;i < genstande.size(); i++) {
			if (emner.contains(genstande.get(i).getEmne()))
				emner.remove(genstande.get(i).getEmne());
		}
		// nu kun emner uden genstande tilbage i listen emner
		
		List<GenstandObjectIF> retur = new ArrayList<GenstandObjectIF>(genstande.size() + emner.size());
		for (GenstandObjectIF g : genstande) {
			if (pInclAnnullerede || !g.isAnnulleret()) 
				retur.add(g);
		}
		for (GenstandObjectIF g : emner) {
			if (pInclAnnullerede || !g.isAnnulleret()) retur.add(g);
		}
		Collections.sort(retur, GenstandObjectIF.GENSTANDOBJECT_COMPARATOR); // sekvens-sortering
		return retur;
	}
	
	@SuppressWarnings("unchecked")
	public List<GenstandObjectIF> getGenstandsObjekterGld(BigDecimal pDato){
		List<GenstandObjectIF> lstGenstandObjIf = getGenstandsObjekter(pDato);
		lstGenstandObjIf = Datobehandling.findGaeldende(lstGenstandObjIf, pDato);
		return lstGenstandObjIf;
	}
	
	@SuppressWarnings("unchecked")
	public List<GenstandObjectIF> getGenstandsObjekter(BigDecimal pDato){
		List<Genstand> lstGenstande = null;
		List<Emne> lstEmner = null;
		if (pDato != null) {
			Genstand[] gns = (Genstand[]) DBServer.getInstance().getVbsf().get(this, getOpgDatoKorrigeret(pDato), "Genstand");
			lstGenstande = Datobehandling.findGaeldendeOgFremtidige(ContainerUtil.asList(gns), pDato);
			lstEmner = ContainerUtil.asList(getEmneGldFremtidige(pDato));
		} else {
			lstGenstande = ContainerUtil.asList(getGenstand());
			lstEmner = ContainerUtil.asList(getEmne());
		}
		return getGenstandsObjekter(lstGenstande, lstEmner, false);
	}
	/**
	 * 
	 * returnerer gældende og fremtidige Emner eller Genstande (udfra pDato) der har gældende dækninger på sig (udfra dagsdato)
	 * returneres som GenstandObjectIF
	 * @param pDato
	 * @return GenstandObjectIF
	 *
	 */
	public List<GenstandObjectIF> getGenstandsObjekterMedGaeldendeOgFremtidigeDaekninger(BigDecimal pDato){
		List<GenstandObjectIF> genstandsObjekter = getGenstandsObjekter(pDato);
		List<GenstandObjectIF> genstandsObjekterExclRemovede = new ArrayList<GenstandObjectIF>();
		for (int i = 0; genstandsObjekter != null && i < genstandsObjekter.size(); i++) {
	        Produkt[] daekningerGld = genstandsObjekter.get(i).getGaeldendeOgFremtidigeDaekninger(Datobehandling.getDagsdatoBigD());
	        if(daekningerGld != null && daekningerGld.length > 0) {
	        	genstandsObjekterExclRemovede.add(genstandsObjekter.get(i));
	        }
        }
		return genstandsObjekterExclRemovede;
	}

	// /**
	// * wrappermetode som omformer Object til Produkt
	// *
	// */
	// // kunne ikke få typecast til at virke
	// public ArrayList getGenstandsObjekter(Object[] produkter) {
	// if (produkter != null && produkter.length > 0) {
	// if(produkter[0] instanceof Produkt) {
	// Produkt[] p = new Produkt[produkter.length];
	// for (int i = 0; i < produkter.length; i++) {
	// p[i] = (Produkt)produkter[i];
	// }
	// return this.getGenstandsObjekter(p);
	// } else if(produkter[0] instanceof Hovedprodukt){
	// return getGenstandsObjekter((Hovedprodukt)produkter[0]);
	// }
	// }
	// return null;
	// }

	// /**
	// * Returner ud fra dækningsliste alle genstande/emer på en aftale uanset dato.
	// * Hvis en dækning hænger direkte på emne returneres en forekomst pr. emne
	// * Udviklet specielt aht. Aftale/gui hvor et genstandsObjekt er en væsentlig
	// * gui-komponent.
	// * Bemærk at resultatet ikke umiddelbart kan datokontrolleres, da specielt emner ikke
	// * har nogen ophørsdato, selvom alle dækninger er ophørt.
	// *
	// */
	// public ArrayList getGenstandsObjekter(Produkt[] daekninger) {
	// if (daekninger == null) return null; // aftale uden dækninger
	// ArrayList gnObjList = new ArrayList();
	// ProduktImpl d;
	// GenstandObjectIF gnObj = null;
	//
	// for (int i = 0; i < daekninger.length; i++) { // for hver dækning på aftalen
	// d = (ProduktImpl)daekninger[i];
	// gnObj = d.getGenstand();
	// if (gnObj == null) { // ingen genstand, så må vi håbe på et emne
	// gnObj = d.getEmne();
	// }
	// if (gnObj != null) {
	// if (!gnObjList.contains(gnObj)) {
	// gnObjList.add(gnObj);
	// }
	// }
	// }
	// gnObjList.trimToSize();
	// return gnObjList;
	// }

	// /**
	// */
	// public ArrayList getGenstandsObjekter(Hovedprodukt pHovedprodukt) {
	// ProduktEmne[] pdem = (ProduktEmne[])ContainerUtil.arraycopy(pHovedprodukt.getProduktEmne(),ProduktEmne.class);
	// ArrayList result = null;
	// ArrayList emner = new ArrayList();
	// if(pdem != null && pdem.length > 0) {
	// for (int i = 0; i < pdem.length; i++) {
	// if(!emner.contains(pdem[i].getEmne()))
	// emner.add(pdem[i].getEmne());
	// }
	// for (int i = 0; i < emner.size(); i++) {
	// EmneGenstand[] emgn = (EmneGenstand[])ContainerUtil.arraycopy(((Emne)emner.get(i)).getEmneGenstand(),
	// EmneGenstand.class);
	// if(emgn != null && emgn.length > 0) {
	// if(result == null)
	// result = new ArrayList();
	// for (int j = 0; j < emgn.length; j++) {
	// if(!result.contains(emgn[j].getGenstand()))
	// result.add(emgn[j].getGenstand());
	// }
	//
	// } else {
	// if(result == null)
	// result = new ArrayList();
	// result.add(emner.get(i));
	// }
	// }
	// }
	// return result;
	// }

	/**
	 * Afgører om en aftale er fremtidig i forhold til dags dato En annulleret aftale er aldrig fremtidig uanset
	 * gældende dato
	 */
	public boolean isFremtidig() {
		return (!isAnnulleret() && super.isFremtidig());
	}
	
	/**
	 * Undersøger om aftalen er forsikringsaftale og gældende på medsendte dato eller bliver gældende senere.<p>
	 * 
	 * Bemærk at metoden ikke svarer på om this er en forsikring. Den svarer på om aftalen har dækninger og dermed er en
	 * dækningsløs forsikring ikke en forsikring<p>
	 * 
	 * OBS: bør ikke anvendes ved tunge kørsler eller svartidskritiske funktioner, da der loades mange data.<br>
	 * Overvej at bruge {@link this.getAftaleTypen().isForsikringsAftaletype()} sammen med {@link this.isGldEllerFremtidig(pDato)}
	 * 
	 * @return false hvis aftale ikke har en dækning som er gældende på medsendte dato eller bliver gældende senere, ellers true
	 */
	public boolean isForsikringsAftaleGldEllerFremtidig(BigDecimal pDato) {
		if (!this.isGldEllerFremtidig(pDato)){
			return false;
		}
		
		Produkt[] daekninger = this.getDaekninger();
		if (daekninger != null && daekninger.length > 0){
			for (int i = 0; i < daekninger.length; i++) {
				if (!daekninger[i].isAnnulleret() && (daekninger[i].isGld(pDato)|| daekninger[i].isFremtidig())
						&& daekninger[i].isGrundprodukt()){
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Undersøger om aftalen er forsikringsaftale og gældende på medsendte dato.
	 * 
	 * @return false hvis aftale ikke har en dækning som er gældende på medsendte dato, ellers true
	 */
	public boolean isForsikringsAftaleGld(BigDecimal pDato) {
		if (!this.isGld(pDato)){
			return false;
		}
		
		Produkt[] daekninger = this.getDaekninger();
		if (daekninger != null && daekninger.length > 0){
			for (int i = 0; i < daekninger.length; i++) {
				if (!daekninger[i].isAnnulleret() && daekninger[i].isGld(pDato)
						&& daekninger[i].isGrundprodukt()){
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Undersøger om aftalen er forsikringsaftale.
	 * 
	 * @return false hvis aftale ikke er forsikringsaftale, ellers true
	 */
	public boolean isForsikringsAftale() {
		return this.getAftaleTypen().isForsikringsAftaletype();
	}

	/**
	 * Undersøger om aftalen er gældende på medsendte dato eller bliver gældende senere.
	 * 
	 * @return false for annullerede forsikringer eller forsikringer ophørt inden datoen, ellers true
	 */
	public boolean isGldEllerFremtidig(BigDecimal pDato) {
		if (this.isAnnulleret()) {
			return false;
		}
		if (!this.isOphUdfyldt() || this.getOph().compareTo(pDato) >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Undersøger om aftalen er gld. på medsendte dato.
	 */
	public boolean isGld(BigDecimal pDato) {
		if (!super.isGld(pDato))
			return false;

		// er altså gældende og kan ikke være annulleret

		if (pDato.compareTo(Datobehandling.getDagsdatoBigD()) < 0) {
			if (this.isLukketKonverteretAftale() == false)
				return true;
			else
				return false;
		}
		return true;
	}

	/**
	 * Afgører om en aftale er annulleret. Hvis aftalens har en ophørsdato, checkes om denne ligger før aftalens gld.
	 * Hvis aftalen ingen oph. dato har, kan den være 'annulleret på vej', dvs. annulleringen er registreret, men
	 * aftalen er ikke tariferet endnu. Derfor skal der checkes på status = isAnnullertPaaVej ("godk.annul") En aftale
	 * som har annullering registreret men endnu ikke er tariferet betragtes som at være annulleret. 
	 * 
	 * @see dk.gensam.gaia.model.aftale.StatustypeImpl#isAnnulleretPaaVejStatus()
	 */
	public boolean isAnnulleret() {
		if (isOphUdfyldt()) {
			if (this.getGld().compareTo(this.getOph()) > 0)
				return true;
		}
		else if(super.isFremtidig() && getAftaleStatustypeNyesteUdenOphoer() != null &&
				 getAftaleStatustypeNyesteUdenOphoer().getStatustype().isAnnulleretPaaVejStatus()){
			return true;
			/*
			 * 14.08.2002 Har
			 * besluttet ikke at aflæse statustype. En aftale er ikke annulleret før ophørsdatoen tilsiger det. Mens
			 * "AnnulleretPåVej" er aftalen nu LÅST, da den sluttariferes straks.
			 * 16.10.2008 PK, LF
			 * Besluttet at genoplive, da vi ellers ikke understøtter fremtidige annulleringer, der bare ikke er annulleret
			 * pga antal afviklingsdage.
			 * 
			 */
		}
		return false;
	}

	@Override
	public boolean isRegistreretAnnulleret() {
		// Supplement til isAnnulleret().
		// Det sidste i tariferingen er ophør og udførtmarkering, men den er annulleret hele vejen
		AfPdAnnullering[] afPdAnnulleringTab = this.getAfPdAnnullering();
		if (afPdAnnulleringTab != null) {
			for (AfPdAnnullering afPdAnnullering : afPdAnnulleringTab) {
				if (!(afPdAnnullering.isUdfoertmarkeringj_n()) && afPdAnnullering.isAftaleannulleretj_n()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Undersøger om aftalen må ændres. En annulleret eller konverteret aftale kan ikke ændres. OBS! Metoden anvender
	 * IKKE dato.
	 */
	public boolean maaAftalenAendres() {
		if (this.isAnnulleret())
			return false;
		if (this.isLukketKonverteretAftale())
			return false;
		return true;
	}

	/**
	 * @return true hvis isAnnulleret ellers super.isOphoert(d)
	 * @since v1.6 25.08.2006 Overskrevet super med check på isAnnuleret()
	 */
	public boolean isOphoert(BigDecimal pOpgDato) {
		if (this.isAnnulleret()) {
			return true;
		}
		return super.isOphoert(pOpgDato);
	}

	/**
	 * Afgører om en aftale må revideres. Konverterede ophørte aftaler må aldrig kunne ændres/revideres
	 * 
	 */
	// public boolean isMaaRevideres() {
	// return true; // @todo mangler endelig beskrivelse
	// }
	/**
	 * Afgør om denne afkomponenttype overhovedet kan have relationer tilknyttet
	 */
	public boolean isRelationerMulig() {
		return true;
	}

	@Override
	public boolean isLukketKonverteretAftale() {
		if (isOphoert() && this.getOprBruger().trim().equals("KONV") && this.harStatusOphoert()) {
			// Hvad skal der ske hvis der ingen statustyper er?
			AftaleStatustype[] typer = this.getAftaleStatustypeUdenOphoer();
			if (typer != null && typer.length > 0) {
				for (int i = 0; i < typer.length; i++) {
					if (typer[i].getOph().intValue() == 0) {
						// Hvis der findes en statustype på aftalen som ikke er sat til ophør,
						// er aftalen ikke en lukket konverteret aftale.
						return false;
					}
				}
			}
			// Returnere true hvis aftalen en ophørt og oprBruger = KONV... (men hvis der ikke fandtes
			// nogle statustyper på aftalen men de andre kriterier er opfyldt, returneres også true).
			return true;
		}
		AftaleStatustype[] konvluk = this.getAftaleStatustype(StatustypeImpl.getStatusType("KONVLUK"), true);
		if (konvluk != null && konvluk.length > 0)
			return true;
		return false;
	}

	// /**
	// * Afgører om aftalen er sluttariferet.
	// * Hvis aftalens statustype er = "Ophørt" eller "Godk.oph" eller "Oph.ikraft" er aftalen sluttariferet - men kan
	// egentlig stadig være
	// * gld... dvs. kunden _kan_ være dækket.
	// *
	// * @see dk.gensam.gaia.model.aftale.StatustypeImpl#isSluttariferetStatus()
	// */
	// public boolean isSluttariferet(){
	// if(getAftaleStatustype() != null && getAftaleStatustype().getStatustype().isSluttariferetStatus())
	// return true;
	// return false;
	// }
	/**
	 * 
	 * @return true hvis aftalen har status = Ophørt
	 */
	public boolean harStatusOphoert() {
		AftaleStatustype as = getAftaleStatustypeNyesteUdenOphoer();
		return (as != null && as.getStatustype().isOphoertStatus());
	}
	
	// Hvis denne metode genoplives, bør isGld reimplementeres - giver ingen mening ved afsttp
//	/**
//	 * @return true hvis aftalen har statustypen "TILBUD" eller en statustype der begynder med "TILBUD"
//	 */
//	public boolean harStatusTilbudInclOphoer() {
//		AftaleStatustype[] aftaleStatsutyper = getAftaleStatustypeAlle();
//		for (AftaleStatustype aftaleStatustype : aftaleStatsutyper) {
//	        if (aftaleStatustype.getStatustype().getKortBenaevnelse().contains("TILBUD") && 
//	        	aftaleStatustype.getStatustype().isGld(Datobehandling.getDagsdatoBigD())) {
//	        	return true;
//	        }
//        }
//		return false;
//	}
	
//    public boolean harStatusTilbudUnderBehandling() {
//    	AftaleStatustype aftaleStatustype = getAftaleStatustypeNyesteUdenOphoer();
//    	return aftaleStatustype==null?false:aftaleStatustype.getStatustype().getKortBenaevnelse().contains("TILBUD");
//   }

	
	/**
	 * @return true hvis aftalen KUN har statustyper der indeholder "TILBUD", ellers false.
	 *
     * @deprecated
     * @see isTilbud
	 */
	public boolean harKunTilbudsstatustyper() {
		if (true)
			return isTilbud();
		String rel = AftaleStatustypeImpl.class.getName();
		String type = StatustypeImpl.class.getName();
		
		AggregateOQuery qry = new AggregateOQuery(AftaleStatustypeImpl.class);
		qry.addJoin(rel+".Statustype", type+".statustypeId");
		qry.add(this.getId(), "Aftale");
		
		int countAlle = QueryService.getCount(qry);
		if (countAlle <= 0)
			return false;
		
		qry.add("%"+Statustype.STATUS_TYPE_TILBUD+"%", type+".statustypeforkortelse", OQuery.NOT_LIKE );
		
//		qry.addAggregate("*", AggregateOQuery.COUNT);
		
		int countOevrige = QueryService.getCount(qry);
		return countOevrige <= 0;
		
//		AftaleStatustype[] aftaleStatsutyper = getAftaleStatustypeAlle();
		
//		if (aftaleStatsutyper == null) return false;
//		
//		for (AftaleStatustype aftaleStatustype : aftaleStatsutyper) {
//	        if (!(aftaleStatustype.getStatustype().getKortBenaevnelse().contains("TILBUD"))) {
//	        	return false;
//	        }
//        }
//		return true;
	}
//	public boolean harKunTilbudsstatustyperNewStyle() {
////		String rel = AftaleStatustypeImpl.class.getName();
////		String type = StatustypeImpl.class.getName();
//		if (!isForsikringsAftale())
//			return false;
//		
//		AggregateOQuery qry = new AggregateOQuery(AftaleStatustypeImpl.class);
////		qry.addJoin(rel+".Statustype", type+".statustypeId");
//		qry.add(this.getId(), "Aftale");
//		qry.add(StatustypeImpl.getNonTilbudAsIds(), "Statustype", OQuery.IN);
//		
////		int countAlle = QueryService.getCount(qry);
////		if (countAlle <= 0)
////			return false;
////		
////		qry.add("%"+Statustype.STATUS_TYPE_TILBUD+"%", type+".statustypeforkortelse", OQuery.NOT_LIKE );
//		
////		qry.addAggregate("*", AggregateOQuery.COUNT);
//		
//		int countOevrige = QueryService.getCount(qry);
//		return countOevrige <= 0;
//	}
	/**
	 * Denne metode har kun effekt på aldrig gemte aftaler der har uinitieret attribut aftaleKategori.<br>
	 * Metoden SKAL ikke kaldes, da persistenslaget retter op efter første commit. Metoden skal kaldes ved instantiering, 
	 * hvis man har brug for at kende kategorien inden første commit.
	 * 
	 * @param aftalekat
	 * @see aftaleKategori
	 */
	public void setAftaleKategoriWhenNew(Integer aftalekat) {
		if (this.isNytObjekt() && aftaleKategori == null) {
			aftaleKategori = aftalekat;
		}
	}
	
	public boolean isTilbud() {
		return aftaleKategori != null && aftaleKategori == 1;
	}
	
	/**
	 * 
	 * @return true hvis aftalen er en forsikring og helt sikkert ikke et tilbud<br>
	 * Svarer også true for reas-aftaler o.a. med aftalestatus
	 */
	public boolean isForsikringAttr() {
		return aftaleKategori != null && aftaleKategori == 2;
	}
	
	/**
	 * 
	 * @return true hvis aftale ingen aftalestatus har overhovedet.
	 */
	public boolean isNoForsikringAttr() {
		return aftaleKategori == null || aftaleKategori < 1 || aftaleKategori > 2;
	}

	/**
	 * 
	 * @return true hvis aftalen har status = Tilbud
	 */
	public boolean harStatusTilbud() {
		if (!isTilbud())
			return false;
		AftaleStatustype as = getAftaleStatustypeNyesteUdenOphoer();
		return (as != null && as.getStatustype().isTilbud());
	}
//	/**
//	 * 
//	 * @return svaret på om nyeste status er en af tilbudstyperne
//	 */
//	public boolean isTilbud(){
////		return isTilbudAttr();
//		AftaleStatustype as = getAftaleStatustypeNyesteUdenOphoer();
//		return as != null && as.getStatustype().isEnAfTilbudTyper();
//	}
	/**
	 * @return svaret på om aftalen har status af tilbud, som stadig er åben/aktiv/i live
	 */
	public boolean isTilbudAaben(){
		return tjekTilbudStatus(Statustype.STATUS_TYPE_TILBUD, Statustype.STATUS_TYPE_TILBUD_ACC);
	}
	
	@Override
	public boolean isTilbudUdloebet(){
		return tjekTilbudStatus(Statustype.STATUS_TYPE_TILBUD_UDL);
	}

	public boolean tjekTilbudStatus(String... statusTypeKortBenaevnelser) {
		if (!isTilbud())
			return false;

		AftaleStatustype aftaleStatustypeNyesteUdenOphoer = getAftaleStatustypeNyesteUdenOphoer();
		if (aftaleStatustypeNyesteUdenOphoer != null) {
			Statustype sttp = aftaleStatustypeNyesteUdenOphoer.getStatustype();

			if (sttp != null && sttp.isEnAfTilbudTyper()) {
				String krtb = sttp.getKortBenaevnelse().trim();
				if (statusTypeKortBenaevnelser != null) {
					for (String stKortBenaevnelse : statusTypeKortBenaevnelser) {
						if (krtb.equals(stKortBenaevnelse)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Svarer på om blot een af aftalens dækninger har en ikke-udført tariferingsopgave pr. datoen
	 * 
	 * @param pDato
	 * @return true hvis blot een dækning har ikke-udført tariferingsopgave pr. datoen ellers false
	 */
	public boolean harTariferingsopgaverIkkeUdfoerte(BigDecimal pDato) {
		RegelsaetType opgtp = QueryService.lookupRegelType(OpgavetypeImpl.class, "TARIFERING");
		String[] parm = { opgtp.getId(), getAftaleId(), pDato.toString() };
		BigDecimal count = QueryService.getValueSingle("AftaleIkkeUdfoerteDagbogProdukt", ContainerUtil.asList(parm));
		return count != null && count.intValue() > 0;
	}

	/**
	 * 
	 * @return true hvis aftalens isSluttariferingUdfoert() er sand eller har status = Godkendt Ophør, Ophør ikraft
	 */
	public boolean isSluttariferingIgangsat() {
		AftaleStatustype as = getAftaleStatustypeNyesteUdenOphoer();
		return (as != null && as.getStatustype().isSluttariferingIgangsat());
	}

	/**
	 * Afgør om aftalen nogensinde har været tariferet. En række ting er ikke muligt at ændre på en aftale efter første
	 * tarifering Algoritmen anbefalet af Long
	 */
	public boolean isTariferet() {
		if (!isTariferet_ && !getId().equals("")) {
			// DBFactory.queryInit();
			OQuery qry = QueryService.queryCreate(ProduktImpl.class);
			QueryService.queryAdd(qry, getId(), "Aftale", QueryService.EQUAL);
			qry.addJoin(ProduktImpl.class.getName()+".produktId", ProduktpraemieImpl.class.getName()+".Produkt");
			qry.setMaxCount(1);
			Object[] aftaleTariferet = DBServer.getInstance().getVbsf().queryExecute(qry);

			if (aftaleTariferet != null) {
				isTariferet_ = true;
			}
		}
		return isTariferet_;
	}

	public boolean isTariferetPrDatoOgBlokererGentagetFornyelse(BigDecimal pTarifDato) {
		OQuery qry = QueryService.queryCreate(ProduktImpl.class);
		qry.add(getId(), "Aftale", OQuery.EQUAL);
		qry.addJoin(ProduktImpl.class.getName()+".produktId", ProduktpraemieImpl.class.getName()+".Produkt");
		qry.add(pTarifDato, ProduktpraemieImpl.class.getName()+".gld", OQuery.EQUAL);
		qry.add(BigDecimal.ZERO, ProduktpraemieImpl.class.getName()+".praemiebeloeb", OQuery.GREATER);
		qry.setMaxCount(1);
		Object[] aftaleTariferet = DBServer.getInstance().getVbsf().queryExecute(qry);
		return (aftaleTariferet != null);
	}

	public IndividAftale[] getIndividAftale() {
		return (IndividAftale[]) DBServer.getInstance().getVbsf().get(this, "IndividAftale");
	}

	/** Relationer mellem aftalen og individer. */
	public IndividAftale[] getIndividAftale(BigDecimal dato) {
		return (IndividAftale[]) DBServer.getInstance().getVbsf().get(this, "IndividAftale", dato);
	}

	/** 
	 * Metoden returnerer de individer, der er registreret som forsikringstager og evt. medejer(e) / medforsikrede på
	 * forsikringen.
	 * 
	 * @param pDato
	 * @param pInclMedforsikrede true hvis metoden skal returnere medforsikrede på genstandsniveau
	 * @return List<Individ> hvor den første altid er forsikringstager og de næste individer med
	 * forholdsbeskrivelsen Medejer eller (optional) Medforsikret
	 */
	public List<Individ> getEjere(BigDecimal pDato, boolean pInclMedforsikrede) {
		return getEjere(pDato, pInclMedforsikrede, false);
	}
	/**
	 * 
	 * @param pDato
	 * @param pInclMedforsikrede
	 * @param kunReeltForsikrede
	 * @return liste over ejere/forsikrede
	 */
	private List<Individ> getEjere(BigDecimal pDato, boolean pInclMedforsikrede, boolean kunReeltForsikrede) {
		List<Individ> ejere = new ArrayList<>(2);
		List<Genstand> genstande = this.getGenstandeGld(pDato);
		if (!kunReeltForsikrede){
			ejere.add(this.getTegnesAfIndivid());

			IndividAftale[] relationer = this.getIndividAftale(pDato);
			for (int i = 0;relationer != null && i < relationer.length; i++) {
				IntpAftpFhbsk smh = (IntpAftpFhbsk)relationer[i].getForholdsbeskrivelse();
				if (smh.isMedejer()){
					Individ r = relationer[i].getIndivid();
					if (!ejere.contains(r))
						ejere.add(r);
				}
			}
			// Nu også søg på Medejer på genstande
			if (genstande != null){
				for (Genstand genstand : genstande) {
					InGnforhold[] rels = genstand.getRelationer(pDato);
					if (rels != null){
						for (InGnforhold rel : rels) {
							if (rel.getIntpGntpsmhbsk().isMedejer()){
								Individ r = rel.getIndivid();
								if (!ejere.contains(r))
									ejere.add(r);
							}
						}
					}
				}
			}

		}
		if (pInclMedforsikrede){
		// ULY? En evt. medforsikret er at betragte som ejer.
			if (genstande != null){
				for (Genstand genstand : genstande) {
					InGnforhold[] rels = genstand.getRelationer(pDato);
					if (rels != null){
						for (InGnforhold rel : rels) {
							if (rel.getIntpGntpsmhbsk().isForsikretVoksen()){
								Individ r = rel.getIndivid();
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

	public List<Individ> getForsikredeVoksneBoern(BigDecimal pDato) {
		List<Individ> forsikrede = new ArrayList<>();
		List<Genstand> gns = this.getGenstandeGld(pDato);
		if (gns != null){
			for (Genstand genstand : gns) {
				InGnforhold[] rels = genstand.getRelationer(pDato);
				if (rels != null){
					for (InGnforhold rel : rels) {
						if (rel.getIntpGntpsmhbsk().isForsikret()){ // voksen , barn
							Individ r = rel.getIndivid();
							if (!forsikrede.contains(r))
								forsikrede.add(r);
						}
					}
				}
			}
		}
		return forsikrede;
	}

	public IndividAftale[] getIndividAftale(boolean maegler, BigDecimal dato) {

		IndividAftale[] ia = null;

		// Hvis medsendte dato er null, loades alle individAftaler
		if (dato == null)
			ia = getIndividAftale();
		else if (dato != null)
			ia = getIndividAftale(dato);

		ArrayList<IndividAftale> result = new ArrayList<IndividAftale>();

		for (int i = 0; ia != null && i < ia.length; i++) {
			if (ia[i].getIntpAftpFhbsk().isMaegler() == maegler) {
				result.add(ia[i]);
			}
		}

		return ContainerUtil.toArray(result);
	}

	/**
	 * konv-metode til HF v7.1
	 */
	public IndividAftale[] getIndividAftaleANDENPM(IntpAftpFhbskImpl ANDENPM, BigDecimal dato) {

		IndividAftale[] ia = null;

		// Hvis medsendte dato er null, loades alle individAftaler
		if (dato == null)
			ia = getIndividAftale();
		else if (dato != null)
			ia = getIndividAftale(dato);

		ArrayList<IndividAftale> result = new ArrayList<IndividAftale>();

		for (int i = 0; ia != null && i < ia.length; i++) {
			if (ia[i].getIntpAftpFhbsk().equals(ANDENPM)) {
				result.add(ia[i]);
			}
		}

		return ContainerUtil.toArray(result);
	}

	public IndividAftale[] getIndividAftaleGldOgFremtidige(boolean maegler, BigDecimal dato) {
		return (IndividAftale[]) Datobehandling.findGaeldendeOgFremtidige(getIndividAftale(maegler, null), dato);
	}

	public RelationsHolderRelationIF[] getFremtidigeMaegler(BigDecimal pDato) {
		return ContainerUtil.arraycopy(Datobehandling.findFremtidige(getIndividAftale(true, null), pDato),
				RelationsHolderRelationIF.class);
	}

	public RelationsHolderRelationIF[] getFremtidigeRelationer(BigDecimal pDato) {
		return ContainerUtil.arraycopy(Datobehandling.findFremtidige(getIndividAftale(false, null), pDato),
				RelationsHolderRelationIF.class);
	}

	public RelationsHolderRelationIF[] getFremtidigePanthavere(BigDecimal pDato) {
		return null;
	}

	/**
	 * Metoden undersøger aftalen har en panthaver tilknyttet pr. pDato. Aftalens gld. genstande på undersøges for
	 * panthavertilknytning, og der returneres true med det samme efter den første fundne.
	 * 
	 * @param pDato
	 * @return true hvis bare én genstand har panthaver tilknytning pr. pDato
	 */
	public boolean harPanthaverTilknytning(BigDecimal pDato) {
		ArrayList<?> genstande = this.getGenstandeGld(pDato); // Find gld. genstande pr. pDato
		if (genstande != null && genstande.size() > 0) {
			for (int i = 0; i < genstande.size(); i++) {
				if (genstande.get(i) instanceof Genstand) {
					if (((Genstand) genstande.get(i)).getAntalPanthavere(pDato) > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Metoden undersøger aftalen har en panthaver tilknyttet pr. pDato og fremefter. Aftalens gld. genstande på undersøges for
	 * panthavertilknytning, og der returneres true med det samme efter den første fundne.
	 * 
	 * @param pDato
	 * @return true hvis bare én genstand har panthaver tilknytning pr. pDato
	 */
	public boolean harPanthaverTilknytningInclFremtid(BigDecimal pDato) {
		ArrayList<?> genstande = this.getGenstandeGld(pDato); // Find gld. genstande pr. pDato
		if (genstande != null && genstande.size() > 0) {
			for (int i = 0; i < genstande.size(); i++) {
				if (genstande.get(i) instanceof Genstand) {
					if (((Genstand) genstande.get(i)).getAntalPanthavereGldOgFremtidige(pDato) > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public Emne[] getEmneGld(BigDecimal pDato) {
		return (Emne[]) DBServer.getInstance().getVbsf().get(this, "Emne", pDato);
//		Datobehandling.findGaeldende(getEmne(), pDato);
	}

	/**
	 * 
	 * @param pDato - gældende fremtidg
	 * @return gældende og fremtidige emner
	 */
	public Emne[] getEmneGldFremtidige(BigDecimal pDato) {
		return (Emne[]) DBServer.getInstance().getVbsf().get(this, pDato, "Emne");
//		Datobehandling.findGaeldende(getEmne(), pDato);
	}
	
	/**
	 * Returnerer et emne af en bestemt type hvis der er tilknyttet et emne af typen til aftalen.
	 */
	public Emne getEmneGld(Emnetype pEmnetype, BigDecimal pDato) {
		Emne[] emner = (Emne[]) Datobehandling.findGaeldende(getEmne(), pDato);
		if ((emner != null) && (emner.length > 0)) {
			for (int i = 0; i < emner.length; i++) {
				if (emner[i].getEmnetype().equals(pEmnetype))
					return emner[i];
			}
		}
		return null;
	}

	/**
	 * Henter aftalens gældende område via aftale/område relationen.
	 */
	public Omraade getOmraade(BigDecimal dato) {
		AftaleOmraade[] ao = (AftaleOmraade[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmraade", dato);

		return (ao != null && ao.length > 0 ? ao[0].getOmraade() : null);
	}

	/**
	 * Henter aftalens gældende relation til Risikosted (Reassurance) <BR>
	 * Der er pr.def. kun eet gældende.
	 */
	public AfOrRisikosted getAfOrRisikosted(BigDecimal pDato) {
		AfOrRisikosted[] afOrRisikostedTabel = (AfOrRisikosted[]) DBServer.getInstance().getVbsf().get(this, "AfOrRisikosted", pDato);
		if ((afOrRisikostedTabel != null) && (afOrRisikostedTabel.length > 0)) {
			return afOrRisikostedTabel[0];
		}
		return null;
	}

	public AfOrRisikosted[] getfremtidigeAfOrRisikosted(BigDecimal pDato) {
		AfOrRisikosted[] afOrRisikostedTabel = getAfOrRisikosted();
		return (AfOrRisikosted[]) Datobehandling.findFremtidige(afOrRisikostedTabel, pDato);
	}

	public AfOrRisikosted[] getAfOrRisikosted() {
		return (AfOrRisikosted[]) DBServer.getInstance().getVbsf().get(this, "AfOrRisikosted");
	}
	/**
	 * @return hovedforfaldsmåned ud fra AftaleFftpMd nyeste, ZERO hvis ingen (korttid)
	 */
	public BigDecimal getHovedforfaldsmaaned(){
		AftaleFftpMd[] rels = getAftaleFftpMd();
		for (int i = 0; rels != null && i < rels.length; i++) {
			if (!rels[i].isOphUdfyldt())
				return rels[i].getMaanedsnummeriaaret();
		}
		return BigDecimal.ZERO;
	}
	/**
	 * 
	 * @return getHovedforfaldsmaaned() as tekst, tom string hvis ingen
	 */
	public String getHovedforfaldsmaanedAsTekst(){
		String s = Datobehandling.getMaanedBenaevnelse(getHovedforfaldsmaaned());
		return s != null ? s : "";
	}

	public AftaleFftpMd[] getAftaleFftpMd() {
		return (AftaleFftpMd[]) DBServer.getInstance().getVbsf().get(this, "AftaleFftpMd");
	}

	public AftaleFftpMd[] getAftaleFftpMd(BigDecimal dato) {
		return (AftaleFftpMd[]) DBServer.getInstance().getVbsf().get(this, "AftaleFftpMd", dato);
	}

	public AftaleFftpMd[] getAftaleFftpMdMedFremtidige(BigDecimal dato) {

		Object[] o = DBServer.getInstance().getVbsf().get(this, "AftaleFftpMd");
		if (o == null)
			return null;
		// den konstruktion giver classcastexception ????
		Object[] o2 = Datobehandling.findGaeldendeOgFremtidige(o, dato);
		if (o2 == null)
			return null;
		AftaleFftpMd[] affk = new AftaleFftpMd[o.length];
		for (int i = 0; i < o2.length; i++) {
			affk[i] = (AftaleFftpMd) o2[i];
		}
		return affk;
	}

	/**
	 * Returnere opkrævningstillægget (% sats) for aftalen.
	 */
	public BigDecimal getAftaleOpkraevningstillaeg(BigDecimal pDato) {
		AftaleFrekvens[] afFrekvens = getAftaleFrekvens(pDato); // Her returneres kun én
		if (afFrekvens != null && afFrekvens[0] != null) {

			Frekvens frekvens = afFrekvens[0].getFrekvens();
			if (frekvens != null) {
				AftpFrekvens aftpFrekvens = getAftaleTypen().getAftaletypeFrekvens(frekvens, pDato);
				if (aftpFrekvens != null) {
					return aftpFrekvens.getOpkraevningstillaeg();
				}
			}
		}
		return null;
	}

	public AftaleFrekvens[] getAftaleFrekvens() {
		return (AftaleFrekvens[]) DBServer.getInstance().getVbsf().get(this, "AftaleFrekvens");
	}

	public AftaleFrekvens[] getAftaleFrekvens(BigDecimal dato) {
		return (AftaleFrekvens[]) DBServer.getInstance().getVbsf().get(this, "AftaleFrekvens", dato);
	}
	public Frekvens getAftalensFrekvens(BigDecimal dato) {
		final AftaleFrekvens[] aftaleFrekvens = getAftaleFrekvens(dato);
		if (aftaleFrekvens != null){
			return aftaleFrekvens[0].getFrekvens();
		}
		return null;
	}

	public AftaleFrekvens[] getAftaleFrekvensMedFremtidige(BigDecimal dato) {
        return (AftaleFrekvens[]) DBServer.getInstance().getVbsf().get(this, dato, "AftaleFrekvens");
	}

	// genererede accessmetoder til AftaleFrekvens

	public void addAftaleFrekvens(AftaleFrekvens pAftaleFrekvens) {
		// autoAdd
	}

	public void removeAftaleFrekvens(AftaleFrekvens oldAftaleFrekvens) {
		// Autoremove
	}

	public AftaleOmraade[] getAftaleOmraade() {
		return (AftaleOmraade[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmraade");
	}

	public void addAftaleFftpMd(AftaleFftpMd pAftaleFftpMd) {
		// autoAdd
	}

	public void removeAftaleFftpMd(AftaleFftpMd oldAftaleFftpMd) {
		// Autoremove
	}

	public void addAftaleAdresse(AftaleAdresse afad) {
		// autoAdd
	}

	public void removeAftaleAdresse(AftaleAdresse afad) {
		// Autoremove
	}

	public void addIndividAftale(IndividAftale individAftale) {
		PersistensService.addToCollection(this, "IndividAftale", individAftale);
	}

	public void removeIndividAftale(IndividAftale individAftale) {
		PersistensService.removeFromCollection(this, "IndividAftale", individAftale);
	}

	public void addAftaleStatustype(AftaleStatustype ast) {
		// auto-addes
	}

	public void removeAftaleStatustype(AftaleStatustype ast) {
		// auto-removes
	}

	// genererede accessmetoder til AfPdAnnullering
	public AfPdAnnullering[] getAfPdAnnullering() {
		return (AfPdAnnullering[]) DBServer.getInstance().getVbsf().get(this, "AfPdAnnullering");
	}

	public void addAfPdAnnullering(AfPdAnnullering pAfPdAnnullering) {
		PersistensService.addToCollection(this, "AfPdAnnullering", pAfPdAnnullering);
	}

	public void removeAfPdAnnullering(AfPdAnnullering oldAfPdAnnullering) {
		PersistensService.removeFromCollection(this, "AfPdAnnullering", oldAfPdAnnullering);
	}

	// genererede accessmetoder til ReasProduktOphoert
	public ReasProduktOphoert[] getReasProduktOphoert() {
		return (ReasProduktOphoert[]) DBServer.getInstance().getVbsf().get(this, "ReasProduktOphoert");
	}

	public void addReasProduktOphoert(ReasProduktOphoert pReasProduktOphoert) {
		PersistensService.addToCollection(this, "ReasProduktOphoert", pReasProduktOphoert);
	}

	public void removeReasProduktOphoert(ReasProduktOphoert oldReasProduktOphoert) {
		PersistensService.removeFromCollection(this, "ReasProduktOphoert", oldReasProduktOphoert);
	}

	// genererede accessmetoder til RrTOrAf
	public RrTOrAf[] getRrTOrAf() {
		return (RrTOrAf[]) DBServer.getInstance().getVbsf().get(this, "RrTOrAf");
	}

	public RrTOrAf[] getRrTOrAf(BigDecimal pDato) {
		return (RrTOrAf[]) DBServer.getInstance().getVbsf().get(this, "RrTOrAf", pDato);
	}

	public void addRrTOrAf(RrTOrAf pRrTOrAf) {
		PersistensService.addToCollection(this, "RrTOrAf", pRrTOrAf);
	}

	public void removeRrTOrAf(RrTOrAf oldRrTOrAf) {
		PersistensService.removeFromCollection(this, "RrTOrAf", oldRrTOrAf);
	}

	public Emne[] getEmne() {
		return (Emne[]) DBServer.getInstance().getVbsf().get(this, "Emne");
	}

	public void addEmne(Emne pEmne) {
		// emnerLoaded_ = false;
		PersistensService.addToCollection(this, "Emne", pEmne);
	}

	public void removeEmne(Emne pEmne) {
		// emnerLoaded_ = false;
		PersistensService.removeFromCollection(this, "Emne", pEmne);
	}

	/**
	 * @return alle aftalens genstande incl. ophørte, fremtidige og annullerede
	 */
	public Genstand[] getGenstand() {
		return (Genstand[]) DBServer.getInstance().getVbsf().get(this, "Genstand");
	}
	/**
	 * @param pGld gældende 
	 * 
	 * @return aftalens gældende genstande pr. den givne dato (som om nødvendigt korrigeres) eller null hvis ingen.
	 * Excl. genstande kun med ophørte dækninger pr. pDato<br>
	 * Har helt samme virkemåde som {@link getGenstandeGldogFremtidige(pDato)} blot uden fremtidige
	 */	
	public List<Genstand> getGenstande(BigDecimal pGld) {
		BigDecimal korrPGld = getOpgDatoKorrigeret(pGld);
		List<Genstand> genstandeGldogFremtidige = getGenstandeGldogFremtidige(korrPGld);
		if (genstandeGldogFremtidige != null && !genstandeGldogFremtidige.isEmpty()) {
			List<Genstand> genstandeGld = new ArrayList<Genstand>(genstandeGldogFremtidige.size());
			for (Genstand genstand : genstandeGldogFremtidige) {
				if (!genstand.isFremtidig(korrPGld)) {
					genstandeGld.add(genstand);
				}
			}
			return genstandeGld;
		}
		// elles returnerer vi bare null eller tom liste 
		return genstandeGldogFremtidige;
	}
	public void addGenstand(Genstand pGenstand) {
		// genstandeLoaded_ = false;
		PersistensService.addToCollection(this, "Genstand", pGenstand);
	}

	public void removeGenstand(Genstand pGenstand) {
		// genstandeLoaded_ = false;
		PersistensService.removeFromCollection(this, "Genstand", pGenstand);
	}

	/**
	 * Afgører om en konkret persistent Aftale er en korttidsforsikring.
	 */
	public boolean isKorttid() {
		// algoritmen er i al væsentlighed overført fra CHKORTAF

		if (!getAftaleTypen().isKorttidTilladt())
			return false;
		// lidt imod principperne at afgøre om noget gælder for data ved at spørge
		// regelsættet om det er tillad - nu.
		// men sådan gør man i CUA og det det mest korrekte i situationen da det ikke
		// kan afgøres på anden måde hvis der ikke er anvendt den dedikerede egenskabsgruppe

		Aftaleegngrp korttidsAegp = (Aftaleegngrp) DBServer.getInstance().getRegelServer().getAftalegrp(Aftaleegngrp.AARSBASERET_PRAEMIE);
		if (korttidsAegp == null)
			return true;
		// hvis egenskabsgruppen ikke er oprettet er aftalen pr. definition korttid nu

		// der er altså en #ÅR-gruppe - er egenskaben så Ja eller Nej ?
//		AftaleAfegn afae = this.getAftaleAfegn(korttidsAegp);
//		if (afae == null)
//			return true;
		Egenskab ae = this.getFelt(korttidsAegp, null);
		if (ae == null)
			return false; // bør ikke kunne ske, men sådan gør man i cua
		if (ae.getBenaevnelse().startsWith("N"))
			return true;

		return false;
	}

	/**
	 * Get bonusbelastende skadesager som ikke har virket bonusbelastende endnu
	 */
	public Skadesag[] getSkadesagerBonusbelastende() {
		List<Comparable> parm = new ArrayList<Comparable>(2);
		parm.add(getId());
		parm.add(BigDecimal.ZERO);

		return (Skadesag[]) DBServer.getInstance().getVbsf().getObjects(SkadesagImpl.setSQLcommandMultijoin(true), parm, 0);
	}

	/**
	 * Get bonusbelastende skadesager uanset om de har virket bonusbelastende endnu eller ej
	 * 
	 * @param pSidenSkadedato
	 *            null eller ZERO = alle
	 */
	public Skadesag[] getSkadesagerBonusbelastende(BigDecimal pSidenSkadedato) {
		List<Comparable> parm = new ArrayList<Comparable>(2);
		parm.add(getId());
		parm.add(pSidenSkadedato != null ? pSidenSkadedato : BigDecimal.ZERO);

		return (Skadesag[]) DBServer.getInstance().getVbsf().getObjects(SkadesagImpl.setSQLcommandMultijoin(false), parm, 0);
	}

	/**
	 * Get antal bonusbelastende skadesager som ikke har virket bonusbelastende endnu
	 */
	public int getSkadesagerBonusbelastendeAntal() {
		List<Comparable> parm = new ArrayList<Comparable>(2);
		parm.add(getId());
		parm.add(BigDecimal.ZERO);

		Object[] o = DBServer.getInstance().getVbsf().getObjects(SkadesagImpl.setSQLcommandMultijoin(true), parm, 0);
		return o != null ? o.length : 0;
	}

	/**
	 * Get antal bonusbelastende skadesager uanset om de har virket bonusbelastende endnu eller ej
	 * 
	 * @param pSidenSkadedato
	 *            null eller ZERO = alle
	 */
	public int getSkadesagerBonusbelastendeAntal(BigDecimal pSidenSkadedato) {
		List<Comparable> parm = new ArrayList<Comparable>(2);
		parm.add(getId());
		parm.add(pSidenSkadedato != null ? pSidenSkadedato : BigDecimal.ZERO);

		Object[] o = DBServer.getInstance().getVbsf().getObjects(SkadesagImpl.setSQLcommandMultijoin(false), parm, 0);
		return o != null ? o.length : 0;
	}
	
	/**
	 * 
	 * Get seneste AftaleTotalkundetype, hvis ingen returneres null.
	 * 
	 */
	public AftaleTotalkundetype getAftaleTotalkundetypeSeneste() {
		
		AftaleTotalkundetype[] aftaleTotalkundetype = this.getAftaleTotalkundetype();
		
		if (aftaleTotalkundetype == null || aftaleTotalkundetype.length == 0) return null;
		
		BigDecimal aftaleTotalkundetypeSenesteDato = GaiaConst.NULBD;
		AftaleTotalkundetype aftaleTotalkundetypeSeneste = null;

		for (AftaleTotalkundetype aftktp : aftaleTotalkundetype) {
			if(aftktp.getGld().compareTo(aftaleTotalkundetypeSenesteDato)== 1) {
				aftaleTotalkundetypeSenesteDato = aftktp.getGld();
				aftaleTotalkundetypeSeneste = aftktp;
			}
		}
		return aftaleTotalkundetypeSeneste;
	}
	
	/**
	 * 
	 * Get startdato fra seneste totalkunde/diplomperiode, hvis ingen periode returneres 0.
	 * 
	 */
	public BigDecimal getAftaleTotalkundetypeDatoSenesteGld() {
		
		AftaleTotalkundetype[] aftaleTotalkundetype = this.getAftaleTotalkundetype();
		
		if (aftaleTotalkundetype == null || aftaleTotalkundetype.length == 0) return GaiaConst.NULBD;
		
		BigDecimal aftaleTotalkundetypeSenesteDato = GaiaConst.NULBD;
		for (AftaleTotalkundetype aftktp : aftaleTotalkundetype) {
			if(aftktp.getGld().compareTo(aftaleTotalkundetypeSenesteDato)== 1) {
				aftaleTotalkundetypeSenesteDato = aftktp.getGld();
			}
		}
		return aftaleTotalkundetypeSenesteDato;
	}
	
	/**
	 * Get antal bonusbelastende skadesager uanset om de har virket bonusbelastende endnu eller ej
	 * fra seneste diplomstart - hvis ikke diplomkunde, returneres 0.
	 * 
	 */
	public int getSkadesagerBonusbelastendeAntalFraAftaleTotalkundetypeDatoSenesteGld() {
		
		BigDecimal aftaleTotalkundetypeDatoSenesteGld = this.getAftaleTotalkundetypeDatoSenesteGld();
		if(aftaleTotalkundetypeDatoSenesteGld.equals(GaiaConst.NULBD)) return 0;
		
		return(this.getSkadesagerBonusbelastendeAntal(aftaleTotalkundetypeDatoSenesteGld));
		
	}
	
	/**
	 * Get antal uafsluttede bonusbelastende skadesager uanset om de har virket bonusbelastende endnu eller ej
	 * fra seneste diplomstart - hvis ikke diplomkunde, returneres 0.
	 * 
	 */
	public int getSkadesagerBonusbelastendeUafslutAntalFraAftaleTotalkundetypeDatoSenesteGld() {
		
		BigDecimal aftaleTotalkundetypeDatoSenesteGld = this.getAftaleTotalkundetypeDatoSenesteGld();
		if(aftaleTotalkundetypeDatoSenesteGld.equals(GaiaConst.NULBD)) return 0;

		Skadesag[] skadesagerBonusbelastende = this.getSkadesagerBonusbelastende(aftaleTotalkundetypeDatoSenesteGld);
		if (skadesagerBonusbelastende == null || skadesagerBonusbelastende.length == 0) {
			return 0;
		}
		int antalUafsluttede = 0;
		for (Skadesag skadesag : skadesagerBonusbelastende) {
			if(!skadesag.isAfsluttet() && !skadesag.isAnnulleret()) {
				antalUafsluttede = antalUafsluttede + 1;
			}
		}
		return antalUafsluttede;
	}
	
	/**
	 * 
	 * @param pPeriode
	 *            (krævet)
	 * @return Skadesager hvor getGld ligger i perioden
	 */
	public Skadesag[] getSkadesager(Periode pPeriode) {
//		if (pPeriode == null)
//			return null;

		String clsSkadekrav = SkadekravImpl.class.getName();
		// String clsSkadekravPd = SkadekravProduktImpl.class.getName();
		String clsProdukt = ProduktImpl.class.getName();
		String clsSkadesag = SkadesagImpl.class.getName();
		// String clsAftaleImpl = AftaleImpl.class.getName();

		// Debug.setDebugging(Debugger.DATABASE, true, false, true, true);

		// String INSQL = "SELECT SKKR.IDENT2 FROM skkr, skkrpd, produkt WHERE SKKR.IDENT= " +
		// " SKKRPD.IDENT and SKKRPD.IDENT2= PRODUKT.IDENT and SKKR.IDENT2 = SKADESAG.IDENT" +
		// " and PRODUKT.IDENT2 = '"+aftaleId+"'";

		// 08.09.2005 MH/LF - ændret join til prepared statement, giver kæmpe performance gevinst.
		OQuery qry = DBServer.getInstance().getVbsf().queryCreate(SkadesagImpl.class);

		if (pPeriode != null) {
			qry.add(pPeriode.getStart(), "gld", OQuery.GREATER_OR_EQUAL, OQuery.AND);
			qry.add(pPeriode.getSlut(), "gld", OQuery.LESS_OR_EQUAL, OQuery.AND);
		}

		qry.add(this.getId(), clsProdukt + ".Aftale", OQuery.EQUAL, OQuery.AND);

		// qry.addJoin(clsAftaleImpl+".aftaleId", clsProdukt+".Aftale");
		qry.addJoin(clsProdukt + ".produktId", clsSkadekrav + ".Produkt");
		// qry.addJoin(clsSkadekravPd+".Skadekrav", clsSkadekrav+".skadekravId");
		qry.addJoin(clsSkadekrav + ".Skadesag", clsSkadesag + ".skadesagId");
		qry.setDistinct(true);

		// qry.add(INSQL, "skadesagId", OQuery.IN, OQuery.AND);

		return (Skadesag[]) DBServer.getInstance().getVbsf().queryExecute(qry);
	}

	/**
	 * @param afeggp Aftaleegngrp.SAGSBEHANDLER,
	 *            eller null hvis metoden selv må finde den.
	 * @return Sagsbehandleregenskab , null hvis ingen
	 * 
	 * @see this.getAftaleEgenskab(Aftaleegngrp pAfeggp)
	 */
	public Egenskab getSagsbehandler(Aftaleegngrp afeggp) {
		if (afeggp == null) {
			afeggp = (Aftaleegngrp) EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.SAGSBEHANDLER, AftaleegngrpImpl.class, OQuery.EQUAL);
		}
		if (afeggp == null) {
			throw new IllegalArgumentException("Selskabet har ikke en " + Aftaleegngrp.SAGSBEHANDLER + " egenskabsgruppe");
		}
		return EgenskabHolderImpl.getEgenskab(this, afeggp, AftaleAfegnImpl.class);
	}
	
	public Egenskab getSagsbehandler() {
		Egenskabsgruppe afeggp = EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.SAGSBEHANDLER, AftaleegngrpImpl.class, OQuery.EQUAL);
		return getFelt(afeggp, null);
	}
	
	public Egenskab getSagsbehandlerEgenskaberLoaded() {
		return getSagsbehandler();
	}

	/**
	 * 
	 * 
	 * @param pPeriode
	 * @return skadersager oprettet i perioden.
	 * @since 27-02-2006 Gensafe Pro 1.5
	 */
	public Skadesag[] getSkadesagerOprettetiPeriode(Periode pPeriode) {
		if (pPeriode == null)
			return null;

		String clsSkadekrav = SkadekravImpl.class.getName();
		// String clsSkadekravPd = SkadekravProduktImpl.class.getName();
		String clsProdukt = ProduktImpl.class.getName();
		String clsSkadesag = SkadesagImpl.class.getName();
		// String clsAftaleImpl = AftaleImpl.class.getName();

		OQuery qry = DBServer.getInstance().getVbsf().queryCreate(SkadesagImpl.class);

		qry.add(pPeriode.getStart(), "oprDato", OQuery.GREATER_OR_EQUAL, OQuery.AND);
		qry.add(pPeriode.getSlut(), "oprDato", OQuery.LESS_OR_EQUAL, OQuery.AND);

		qry.add(this.getAftaleId(), clsProdukt + ".Aftale", OQuery.EQUAL, OQuery.AND);

		// qry.addJoin(clsAftaleImpl+".aftaleId", clsProdukt+".Aftale");
		qry.addJoin(clsProdukt + ".produktId", clsSkadekrav + ".Produkt");
		// qry.addJoin(clsSkadekravPd+".Skadekrav", clsSkadekrav+".skadekravId");
		qry.addJoin(clsSkadekrav + ".Skadesag", clsSkadesag + ".skadesagId");
		qry.setDistinct(true);

		return (Skadesag[]) DBServer.getInstance().getVbsf().queryExecute(qry);
	}

	public String getLabel() {
		return getXOAftaleLabelTxt(Datobehandling.getDagsdatoBigD());
	}

	public String getTegnesAfIndividLabel() {
		return getTegnesAfIndivid().getLabelExtendendedUdenNummer();
	}

	public boolean skiftAftaleStatustypeAfventerGodkendelse(BigDecimal pDato, String statustekst) {
		if (this.isTilbud())
			throw new IllegalStateException("Tilbud kan ikke få status afventer godkendelse ");
		AftaleStatustype aftaleStatustypenMedGld = getAftaleStatustypenMedGld(pDato);
		if (aftaleStatustypenMedGld != null && aftaleStatustypenMedGld.getStatustype().isStoptype()) {
			log_.info("Aftale " + getId() + " får ikke status afventer godkendelse, da den allerede har det pr. " + pDato);
			return false;
		}

		Statustype statusType = StatustypeImpl.getStatusType(Statustype.STATUS_TYPE_AFV_GODKENDT);
		if (getGld().compareTo(pDato) < 0)
			statusType = StatustypeImpl.getStatusType(Statustype.AFVAENDR);
		if (!statusType.isStoptype())
			throw new RegelfejlException(Statustype.STATUS_TYPE_AFV_GODKENDT + " skal være en stoptype");

		udlaegRevidering(pDato);
		// Nu har cobol-program måske oprettet ny aftalestatus og ændret andre
		// Vi skal have refreshed og ophørsmarkeret den nyeste pr. datoen
		fjernAftalestatus(pDato, statusType);
		opretAftaleStatustypeMedTekst(statusType, pDato, statustekst);
		log_.info("Aftale " + getId() + " får status "+statusType.getBenaevnelse()+",  pr. " + pDato);
		return true;
	}

	/**
	 * Sætter øvrige aftalestatus til ophør pr. idag når samme gld og ikke-IKRAFT
	 * @param pDato
	 * @param statusType  den nye der afløser
	 */
	private void fjernAftalestatus(BigDecimal pDato, Statustype statusType) {
		DBServer.getInstance().getVbsf().markCollectionDirty(this, AftaleStatustype.AFTALESTATUSTYPE );
		AftaleStatustype[] aftaleStatustyperMedGld = this.getAftaleStatustyperMedGld(pDato);
		if (aftaleStatustyperMedGld != null) {
			for (AftaleStatustype afsttp : aftaleStatustyperMedGld) {
				if (afsttp.isOphUdfyldt())
					continue;
				if (afsttp.getStatustype().getKortBenaevnelse().startsWith(Statustype.IKRAFT))
					continue;
				afsttp.setOph(Datobehandling.getDagsdatoBigD());
				afsttp.setTilstatustype(statusType);
				PersistensService.save(afsttp);
			}
		}

	}

	public void opretAftaleStatustype(Statustype pStatustype, BigDecimal pDato) {
		AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
		aftaleStatustype.setAftale(this);
		aftaleStatustype.setStatustype(pStatustype);
		aftaleStatustype.setGld(pDato);
		PersistensService.gem(aftaleStatustype);
		addAftaleStatustype(aftaleStatustype);
	}

	public void opretAftaleStatustypeMedTekst(Statustype pStatustype, BigDecimal pDato, String tekst) {
		AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
		aftaleStatustype.setAftale(this);
		aftaleStatustype.setStatustype(pStatustype);
		aftaleStatustype.setGld(pDato);
		aftaleStatustype.setStatustekst(tekst);
		PersistensService.save(aftaleStatustype);
		addAftaleStatustype(aftaleStatustype);
	}

	public void opretAftaleStatustypeOphoert(Statustype pStatustype, BigDecimal pDato, BigDecimal pOphoertDato) {
		AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
		aftaleStatustype.setAftale(this);
		aftaleStatustype.setStatustype(pStatustype);
		aftaleStatustype.setGld(pDato);
		aftaleStatustype.setOph(pOphoertDato);
		PersistensService.gem(aftaleStatustype);
		addAftaleStatustype(aftaleStatustype);
	}

	public void opretAftaleStatustypeKopi(StatusBO pStatusBO) {
		String pNyStatustype = pStatusBO.getType().getKortBenaevnelse().trim();
		AftaleStatustypeImpl asti = ((AftaleStatustypeImpl)pStatusBO.getEntitet());

		// Dette er ikke en kopi men et hack for at få sat ophørsdato/klokkeslet på tilbudstyper der endnu ikke
		// har fået det sat.
		BigDecimal tilDato_ = asti.getOph();
		BigDecimal tilKlokkeslet_ = asti.getOphoersklokkeslet();
		if (tilDato_.intValue() == 0) { 
			tilDato_ = Datobehandling.getDagsdatoBigD(); 
			tilKlokkeslet_ = Datobehandling.getDagsTidBigD();
		}
		
		AftaleStatustype aftaleStatustype = PersistensService.opret(AftaleStatustypeImpl.class);
		aftaleStatustype.setAftale(this);
		aftaleStatustype.setStatustype(StatustypeImpl.getStatusType(pNyStatustype));
		aftaleStatustype.setGld(asti.getGld());
		aftaleStatustype.setOph(tilDato_);
		aftaleStatustype.setOphoersklokkeslet(tilKlokkeslet_);
		aftaleStatustype.setOprBruger(asti.getOprBruger());
		aftaleStatustype.setOprDato(asti.getOprDato());
		aftaleStatustype.setOprTid(asti.getOprTid());
		aftaleStatustype.setStatustekst(asti.getStatustekst());
		
		PersistensService.gem(aftaleStatustype);
		addAftaleStatustype(aftaleStatustype);
	}

	public boolean findesRgtpPaaProdukt(Reguleringstype pReguleringstype, BigDecimal pDato) {
		Produkt[] produktTabel = getDaekningerGld(pDato);
		for (int j = 0; (((produktTabel != null) && (produktTabel.length > 0)) && (j < produktTabel.length)); j++) {
			if (produktTabel[j].findesProduktRgtp(pReguleringstype, pDato)) {
				return true;
			}
		}

		return false;
	}

	// genererede accessmetoder til FiktivAftaleOmkostningstype
	public FiktivAftaleOmkostningstype[] getFiktivAftaleOmkostningstype() {
		return (FiktivAftaleOmkostningstype[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleOmkostningstype");
	}

	public FiktivAftaleOmkostningstype[] getFiktivAftaleOmkostningstype(BigDecimal pDato) {
		return (FiktivAftaleOmkostningstype[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleOmkostningstype", pDato);
		// DBFactory.get(FiktivAftaleOmkostningstype, this, pDato);
	}

	public void addFiktivAftaleOmkostningstype(FiktivAftaleOmkostningstype pFiktivAftaleOmkostningstype) {
		PersistensService.addToCollection(this, "FiktivAftaleOmkostningstype", pFiktivAftaleOmkostningstype);
	}

	public void removeFiktivAftaleOmkostningstype(FiktivAftaleOmkostningstype oldFiktivAftaleOmkostningstype) {
		PersistensService.removeFromCollection(this, "FiktivAftaleOmkostningstype", oldFiktivAftaleOmkostningstype);
	}

	// genererede accessmetoder til FiktivStormflod
	public FiktivStormflod[] getFiktivStormflod() {
		return (FiktivStormflod[]) DBServer.getInstance().getVbsf().get(this, "FiktivStormflod");
	}

	public FiktivStormflod[] getFiktivStormflod(BigDecimal pDato) {
		return (FiktivStormflod[]) DBServer.getInstance().getVbsf().get(this, "FiktivStormflod", pDato);
		// DBFactory.get(FiktivStormflod, this, pDato);
	}

	public void addFiktivStormflod(FiktivStormflod pFiktivStormflod) {
		PersistensService.addToCollection(this, "FiktivStormflod", pFiktivStormflod);
	}

	public void removeFiktivStormflod(FiktivStormflod oldFiktivStormflod) {
		PersistensService.removeFromCollection(this, "FiktivStormflod", oldFiktivStormflod);
	}

	// accessmetoder til Stormflod
	public Stormflod[] getStormflod() {
		return (Stormflod[]) DBServer.getInstance().getVbsf().get(this, "Stormflod");
	}

	public Stormflod[] getStormflod(BigDecimal pDato) {
		// Debug.setDebugging(Debugger.DATABASE+Debugger.DEEP+Debugger.PERSISTENCE);
		return (Stormflod[]) DBServer.getInstance().getVbsf().get(this, "Stormflod", pDato);
		// DBFactory.get(Stormflod, this, pDato);
		// Debug.setDebuggingOff();
	}

	public void addStormflod(Stormflod pStormflod) {
		PersistensService.addToCollection(this, "Stormflod", pStormflod);
	}

	public void removeStormflod(Stormflod oldStormflod) {
		PersistensService.removeFromCollection(this, "Stormflod", oldStormflod);
	}

	// genererede accessmetoder til AftaleRbtpPd
	public AftaleRbtpPd[] getAftaleRbtpPd() {
		return (AftaleRbtpPd[]) DBServer.getInstance().getVbsf().get(this, "AftaleRbtpPd");
	}

	public AftaleRbtpPd[] getAftaleRbtpPd(BigDecimal pDato) {
		return (AftaleRbtpPd[]) DBServer.getInstance().getVbsf().get(this, "AftaleRbtpPd", pDato);
		// DBFactory.get(AftaleRbtpPd,this, pDato);
	}

	public void addAftaleRbtpPd(AftaleRbtpPd pAftaleRbtpPd) {
		PersistensService.addToCollection(this, "AftaleRbtpPd", pAftaleRbtpPd);
	}

	public void removeAftaleRbtpPd(AftaleRbtpPd oldAftaleRbtpPd) {
		PersistensService.removeFromCollection(this, "AftaleRbtpPd", oldAftaleRbtpPd);
	}

	// genererede accessmetoder til FiktivAftaleRbtpPd
	public FiktivAftaleRbtpPd[] getFiktivAftaleRbtpPd() {
		return (FiktivAftaleRbtpPd[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleRbtpPd");
	}

	public FiktivAftaleRbtpPd[] getFiktivAftaleRbtpPd(BigDecimal pDato) {
		return (FiktivAftaleRbtpPd[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleRbtpPd", pDato);
		// DBFactory.get(FiktivAftaleRbtpPd, this, pDato);
	}

	public void addFiktivAftaleRbtpPd(FiktivAftaleRbtpPd pFiktivAftaleRbtpPd) {
		PersistensService.addToCollection(this, "FiktivAftaleRbtpPd", pFiktivAftaleRbtpPd);
	}

	public void removeFiktivAftaleRbtpPd(FiktivAftaleRbtpPd oldFiktivAftaleRbtpPd) {
		PersistensService.removeFromCollection(this, "FiktivAftaleRbtpPd", oldFiktivAftaleRbtpPd);
	}
    /**
     * Finder næste HovedForfald regnet fra pDato - dvs. retur-dato > pDato.
     * 
     * @param pDato
     * @return førstkommende hovedforfaldsdato > pDato (hvis pDato er sammenfaldende med næstehovedforfald returneres altså + 1 år
     */
	public BigDecimal findNaesteHovedForfaldsDato(BigDecimal pDato) {
		if (!isKorttid()) {
			int pDatoInt = pDato.intValue();
			// Find HF-måned
			AftaleFftpMd[] aftaleFftpMdTabel = getAftaleFftpMd(pDato.compareTo(this.getGld()) < 0 ? this.getGld() : pDato);
			int HFmaaned = 0;
			boolean hfMdFundet = false;
			for (int i = 0; (aftaleFftpMdTabel != null) && (!hfMdFundet) && (i < aftaleFftpMdTabel.length); i++) {
				if (aftaleFftpMdTabel[i].getForfaldstype().getId().equals("      1")) { // HF (ID pga. standarddata)
					HFmaaned = aftaleFftpMdTabel[i].getMaanedsnummeriaaret().intValue();
					hfMdFundet = true;
				}
			}

			int pDatoAar = pDatoInt / 10000;
			int wDato = (pDatoAar * 10000) + (HFmaaned * 100) + 1;
			long rtnDato = wDato;
//			BigDecimal rtnDato = new BigDecimal((new Integer(wDato)).doubleValue());
			while (rtnDato <= pDatoInt) {
				rtnDato += 10000; // rtnDato.add(new BigDecimal(10000.0)); // Læg 1 år til (HF-frekvens er altid 1 år)
			}

			return new BigDecimal(rtnDato);
		} else
			return Datobehandling.datoPlusMinusAntal(getOph(), 1);
	}
	
	/**
	 * Tjek om hovedforfald måned er lig med den nuværende ikraft måned
	 * 
	 * @return <code>true</code> hvis månederne er ens ellers <code>false</code>
	 */
	public boolean isHovedForfaldMaanedLigMedIkraftMaaned() {
		if (!isKorttid()) {
			// Find HF-måned
			AftaleFftpMd[] aftaleFftpMdTabel = getAftaleFftpMd(this.getGld());
			BigDecimal HFmaaned = GaiaConst.NULBD;
			boolean hfMdFundet = false;
			for (int i = 0; (aftaleFftpMdTabel != null) && (!hfMdFundet) && (i < aftaleFftpMdTabel.length); i++) {
				if (aftaleFftpMdTabel[i].getForfaldstype().getId().equals("      1")) { // HF (ID pga. standarddata)
					HFmaaned = aftaleFftpMdTabel[i].getMaanedsnummeriaaret();
					hfMdFundet = true;
				}
			}
			
			// Find ikraft-måned
			String p = this.getGld().toString();
			BigDecimal ikraftMd = new BigDecimal(p.substring(4, 6));

			return (HFmaaned.compareTo(ikraftMd) == 0);

		} else
			return false;
	}

	public BigDecimal findSenesteHovedForfaldsDato(BigDecimal pDato) {
	    int talForAtFaaSorteretFaldendePaaGld = 1000;
        DagbogAftale[] dagbogAf = this.getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), null, null, talForAtFaaSorteretFaldendePaaGld);
        for (int i = 0; dagbogAf != null && i < dagbogAf.length; i++) {
            BigDecimal gld = dagbogAf[i].getGld();
            if (gld.compareTo(pDato) <= 0 &&
                    gld.compareTo(getGld()) >= 0) {

                if (i==0){
                    // hvis pDato er meget fremtidig i forhold til første element, korrigerer vi med et antal år for at få fornyelsesdatoen umiddelbart før pDato
                    BigDecimal nxt = gld;
                    while (true) {
                        BigDecimal retur = nxt;
                        nxt = Datobehandling.datoPlusMinusAntalAar(nxt, 1);
                        if (nxt.compareTo(pDato) > 0)
                            return retur;
                    }
                } else {
                    return gld;
                }
            }
        }
		return getGld();
	}

	public BigDecimal findNaesteForfaldsDato(BigDecimal pDato) {
		BigDecimal rtnDato = Datobehandling.datoPlusMinusAntalAar(findNaesteHovedForfaldsDato(pDato), -1); // Næste HF
		// minus 1
		// år

		AftaleFrekvens[] afFkTabel = getAftaleFrekvens(pDato);
		if ((afFkTabel == null) || (afFkTabel[0] == null)) { // Der er kun een
			return null;
		}
		Frekvens frekvens = afFkTabel[0].getFrekvens();
		if ((frekvens.getIntervalangivelseiantalaar().intValue() == 0) && (frekvens.getIntervalangivelseimaaneder().intValue() == 0)) {
			return null;
		}
		while (!(rtnDato.compareTo(pDato) > 0)) {
			if (frekvens.getIntervalangivelseiantalaar().intValue() != 0) {
				rtnDato = rtnDato.add(frekvens.getIntervalangivelseiantalaar().multiply(new BigDecimal(10000.0)));
			}
			if (frekvens.getIntervalangivelseimaaneder().intValue() != 0) {
				rtnDato = Datobehandling.datoPlusMinusAntalMd(rtnDato, frekvens.getIntervalangivelseimaaneder().intValue());
			}
		}
		return rtnDato;
	}
	/**
	 * 
	 * @return det årlige antal betalinger på denne aftale, typisk 1, 2 eller 4. 1 hvis ingen så der kan ganges normalt med frekvensen
	 */
	public int getFrekvensAsAarligeBetalinger(BigDecimal pDato) {
		AftaleFrekvens[] r = getAftaleFrekvens(pDato);
		if (r != null && r.length > 0 ) {
			Frekvens f = r[0].getFrekvens();
			if (f.getIntervalangivelseimaaneder().intValue() > 0){
				return 12 / f.getIntervalangivelseimaaneder().intValue();
			}
			return f.getIntervalangivelseiantalaar().intValue();
		}
		return 1;
	}

	/**
	 * Finder seneste forfald i forhold til medsendte dato. 
	 * Hvis seneste forfald er mindre end aftalens gældende dato, sættes seneste forfalds datoen = aftalens gældende.
	 * 
	 * @since 09-03-2005 Gensafe Pro 1.3
	 * 
	 * @param pDato
	 * @return seneste forfald, eller aftalens gældende dato, hvis seneste forfald < gld.
	 */
	public BigDecimal findSenesteForfaldsDato(BigDecimal pDato) {
//		int subMd = this.getAftaleFrekvens(pDato)[0].getFrekvens().getIntervalAngivelseOmregnetTilMaaneder();
//		subMd = subMd * -1;
//		BigDecimal senesteFF = Datobehandling.datoPlusMinusAntalMd(pDato, subMd);
//		if (senesteFF.compareTo(this.getGld()) < 0) {
//			senesteFF = this.getGld();
//		}
//		return senesteFF;
		BigDecimal rtnDato = findNaesteHovedForfaldsDato(pDato); // Næste HF

		AftaleFrekvens[] afFkTabel = getAftaleFrekvens(pDato);
		if ((afFkTabel == null) || (afFkTabel[0] == null)) { // Der er kun een
			return null;
		}
		Frekvens frekvens = afFkTabel[0].getFrekvens();
		if ((frekvens.getIntervalangivelseiantalaar().intValue() == 0) && (frekvens.getIntervalangivelseimaaneder().intValue() == 0)) {
			return null;
		}
		while ((rtnDato.compareTo(pDato) > 0)) {
			if (frekvens.getIntervalangivelseiantalaar().intValue() != 0) {
				rtnDato = rtnDato.subtract(frekvens.getIntervalangivelseiantalaar().multiply(new BigDecimal(10000.0)));
			}
			
			int intValue = frekvens.getIntervalangivelseimaaneder().intValue();
			if (intValue != 0) {
				rtnDato = Datobehandling.datoPlusMinusAntalMd(rtnDato, -intValue);
			}
		}
		
		if (rtnDato.compareTo(this.getGld()) < 0) {
			rtnDato = this.getGld();
		}
		return rtnDato;
	}

	/**
	 * finder næste forfaldsdato og trækker en dag fra.
	 * 
	 * @since 09-03-2005 Gensafe Pro 1.3
	 * 
	 * @param pDato
	 */
	public BigDecimal findNaesteForfaldsDatoMinusEn(BigDecimal pDato) {
		BigDecimal rtnDato = this.findNaesteForfaldsDato(pDato);
		if (rtnDato == null) {
			return null;
		}
		return rtnDato = Datobehandling.datoPlusMinusAntal(rtnDato, -1);
	}

	/**
	 * finder næste Hovedforfaldsdato og trækker en dag fra.
	 * 
	 * @since 10-03-2005 Gensafe Pro 1.3
	 * 
	 * @param pDato
	 */
	public BigDecimal findNaesteHovedForfaldsDatoMinusEn(BigDecimal pDato) {
		BigDecimal rtnDato = this.findNaesteHovedForfaldsDato(pDato);
		if (rtnDato == null) {
			return null;
		}
		return rtnDato = Datobehandling.datoPlusMinusAntal(rtnDato, -1);
	}

	/**
	 * @return getLabel() omgivet af skrarpe parenteser. F.eks. <code>[567900]</code>.
	 */
	public String toString() {
		return "[" + getLabel() + " " + getAftaletypeKortBenaevnelse() + "]";
	}

//	/**
//	 * Finder nettoPraemiePrDaekning
//	 */
//	public NettoPraemiePrDaekning[] getNettoPraemiePrDaekning() {
//		return (NettoPraemiePrDaekning[]) DBServer.getInstance().getVbsf().get(this, "NettoPraemiePrDaekning");
//
//	}

	/*
	 * public NettoPraemiePrDaekning[] refreshNettoPraemiePrDaekning() { nettoprColl_ = null;
	 * DBFactory.refreshCollection(NettoPraemiePrDaekning, this); return null; }
	 */

	/**
	 * 
	 * @param pDato, null = alle
	 * 
	 * @return alle NettoPraemiePrDaekning[] på aftalen 
	 */
	public NettoPraemiePrDaekning[] getNettoPraemiePrDaekning(BigDecimal pDato) {
		if (pDato != null)
			return (NettoPraemiePrDaekning[]) DBServer.getInstance().getVbsf().get(this, "NettoPraemiePrDaekning", pDato);
		return (NettoPraemiePrDaekning[]) DBServer.getInstance().getVbsf().get(this, "NettoPraemiePrDaekning");
		// DBFactory.get(NettoPraemiePrDaekning, this, pDato);
	}

	public void addNettoPraemiePrDaekning(NettoPraemiePrDaekning pNettoPraemiePrDaekning) {
		PersistensService.addToCollection(this, "NettoPraemiePrDaekning", pNettoPraemiePrDaekning);

	}

	public void removeNettoPraemiePrDaekning(NettoPraemiePrDaekning oldNettoPraemiePrDaekning) {
		PersistensService.removeFromCollection(this, "NettoPraemiePrDaekning", oldNettoPraemiePrDaekning);

	}

	/**
	 * Returnere aftalens nettopræmie pr. given dato inkl. opkrævningstillæg (som er tillæg for halv-årlig, hel-årlig
	 * opkrævning osv)
	 * 
	 * @param pDato
	 * @return aftalens nettopræmie inkl opkrævningstillæg pr pDato om nødvendigt korrigeret til gld eller ophørt.
	 */
	public BigDecimal getNettoPraemieInklOpkTillaeg(BigDecimal pDato) {
		BigDecimal beloeb = GaiaConst.NULBD;
		NettoPraemiePrDaekning[] nppd = this.getNettoPraemiePrDaekning(getOpgDatoKorrigeret(pDato));
		if (nppd != null && nppd.length > 0) {
			for (int i = 0; i < nppd.length; i++) {
				NettoPraemiePrDaekning daekningPraemie = nppd[i];
				beloeb = beloeb.add(daekningPraemie.getNettopraemiebeloebincl_opkraevningstillaeg());
			}
		}
		return beloeb;
	}

	public BigDecimal getAftaleOmkostningerInklStatsafgift(BigDecimal pDato){
		BigDecimal beloeb = GaiaConst.NULBD;

		ProduktOmkostningstypeIF[] pdomkaf = this.findProduktOmkostningstypeIFIkkeStatsafgift(pDato);
		if (pdomkaf != null) {
			for (int i = 0; i < pdomkaf.length; i++) {
				beloeb = beloeb.add(pdomkaf[i].getOmkostningsbeloeb());
			}
		}

		StormflodIF[] stormflod = this.findStormflodIF(pDato);
		if (stormflod != null) {
			for (int s = 0; s < stormflod.length; s++) {
				beloeb = beloeb.add(stormflod[s].getStormflodsbeloeb());
			}
		}

		AftaleAarsAfgiftFordringIF[] aarsAfgift = this.findAftaleAarsAfgiftFordring(pDato);
		if (aarsAfgift != null) {
			for (int b = 0; b < aarsAfgift.length; b++) {
				beloeb = beloeb.add(aarsAfgift[b].getAfgiftsbeloeb());
			}
		}

		// Statsafgift
		Produkt[] produkter = this.getProdukt();
		BigDecimal fordringsAfrundringsGraense = DBServer.getInstance().getSelskabsOplysning().getFordringsAfrundringsGraense();
		for (Produkt produkt : produkter) {
			if (ProdukttypeImpl.isPdtpMedStafg(produkt.getProdukttype())) {
				ProduktOmkostningstypeIF[] statsafgift = produkt.findProduktOmkostningstypeIFStatsafgift(pDato, true, null);
				if (statsafgift != null && statsafgift.length > 0) {
					for (int s = 0; s < statsafgift.length; s++) {
						if (fordringsAfrundringsGraense.compareTo(BigDecimal.ZERO) == 0) {
							beloeb = beloeb.add(statsafgift[s].getOmkostningsbeloeb());
						}
						else {
							beloeb = beloeb.add(statsafgift[s].getOmkostningsbeloeb().divide(
									fordringsAfrundringsGraense).setScale(0, RoundingMode.HALF_UP).multiply(fordringsAfrundringsGraense));
						}
					}
				}
			}
		}
		return beloeb;
	}
	public BigDecimal getProduktPraemieInclBonus(BigDecimal pDato){
		Produkt[] produktGld = this.getProduktGld(pDato);
		BigDecimal svar = BigDecimal.ZERO;
		if (produktGld != null) {
			for (Produkt produkt : produktGld) {
	            svar = svar.add(produkt.getProduktPraemieInclBonus(pDato));
            }
		}
		return svar;
	}
	
	@Override
	public BigDecimal beregnPeriodiseretNettoPraemieInklOpkTillaeg(BigDecimal pPeriodiseringFraDato, BigDecimal pPeriodiseringTilDato) {
		BigDecimal akkumuleretPeriodiseretBeloeb = GaiaConst.NULBD;
		NettoPraemiePrDaekning[] nettoPraemiePrDaekninger = this.getNettoPraemiePrDaekning(null);
		if (nettoPraemiePrDaekninger != null) {
			for (NettoPraemiePrDaekning nettoPraemiePrDaekning : nettoPraemiePrDaekninger) {
				BigDecimal periodiseretBeloeb = nettoPraemiePrDaekning.beregnPeriodiseretNettoPraemieInklOpkTillaeg(pPeriodiseringFraDato,
				        pPeriodiseringTilDato);
				akkumuleretPeriodiseretBeloeb = akkumuleretPeriodiseretBeloeb.add(periodiseretBeloeb);
			}
		}
		return akkumuleretPeriodiseretBeloeb;
	}
	
	@Override
	public BigDecimal[] getPraemieForbrugtOgReserve(BigDecimal fraDato, BigDecimal tilDato) {
		BeloebsBehandling praemieUtilInst_ = new BeloebsBehandling();
		praemieUtilInst_.setFradatoExplicit(fraDato);
		BigDecimal[] perioder = praemieUtilInst_.getPeriodeOpdeling(2, tilDato);
        perioder[1] = fraDato;
		BigDecimal[] praemie = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
		Individ forsTager = getTegnesAfIndivid();
		List<List<Object>> individSummeretPreamie = getSummeretPraemie(forsTager, fraDato, false);
			
		if(individSummeretPreamie != null){
			for (int i = 0; i < individSummeretPreamie.size(); i++){
				List<Object> lin = individSummeretPreamie.get(i);
				String aftaleId = (String)lin.get(0);
				if(aftaleId.equals(getId())){
					BigDecimal[] praefordel = praemieUtilInst_.opdelPrAar(perioder, (BigDecimal)lin.get(1),(BigDecimal)lin.get(2), (BigDecimal)lin.get(3), 10);
					for (int k = 1;praefordel != null && k < praefordel.length; k++) {
						praemie[0] = praemie[0].add(praefordel[k]);
					}
					praemie[1] = praemie[1].add(praemieUtilInst_.getOpsamletPraemieReserve());
				}
			}
		}
		return praemie;

	}
	private List<List<Object>> getSummeretPraemie(Individ pIndivid, BigDecimal fraDato, boolean medKorrektionsFordringer){
		String[] parms_ = new String[2];
		parms_[0] = pIndivid.getId();
		parms_[1] = fraDato.toString();
		String cmdnavn = medKorrektionsFordringer ? "SkadeprocentSummeretPraemie" : "SkadeprocentSummeretPraemieExclKorrektioner";
		return QueryService.getValuesAsListsInListFromCommand(cmdnavn, ContainerUtil.asList(parms_) , 4);
	}

	public BigDecimal getPraemieOpkraevetPeriodiseret(BigDecimal pDatoFra, BigDecimal pDatoTil) {
		BigDecimal antalAar = Datobehandling.antalHeleAar(pDatoFra, pDatoTil);
		int antPerioder = antalAar.intValue() + 1 + 1; // først 1 år hele år + 1. Derefter 1 ekstra periode til de beløb der er fremtidige

		String sql = "SELECT FE.PERIOFRA, FE.PERIOTIL, sum(FE.NETBLB) "+
				"FROM fordring f, fordrext fe, produkt "+
				"WHERE produkt.ident2 = ? and "+
				"FE.PERIOTIL >= ? and "+
				"F.IDENT= FE.IDENT and FE.IDENT3= "+
				"PRODUKT.IDENT AND FE.IDENT1 <> ' ' AND "+
				"(F.IDENT IN (SELECT IDENT2   "+
				"FROM FRPR) OR F.IDENT IN (SELECT IDENT4 FROM FRBN) OR F.IDENT IN "+
				"(SELECT IDENT2 FROM FRRB) OR F.IDENT IN (SELECT IDENT3 FROM FRRBAF) "+
				"OR F.IDENT IN (SELECT IDENT2 FROM FRPRJUST)) "+
				"GROUP BY "+
				"FE.PERIOFRA, FE.PERIOTIL";

		String[] parms_ = new String[2];
		parms_[0] = getId();
		parms_[1] = pDatoFra.toString();

		List<List<Object>> rows = QueryService.getValuesAsListsInListFromSelect(sql, ContainerUtil.asList(parms_), 3);
		BigDecimal resultat = BigDecimal.ZERO;
		if (rows != null) {
			// Helper til beløbsfordeling
			BeloebsBehandling beloebsHelper = new BeloebsBehandling();
			BigDecimal[] perioder = beloebsHelper.getPeriodeOpdeling(antPerioder, pDatoTil);

			for (List<Object> lin : rows) {
				// Resultatsættets 3 kolonner
				BigDecimal praemieFra = (BigDecimal)lin.get(0);
				BigDecimal praemieTil  = (BigDecimal)lin.get(1);
				BigDecimal opkrPrae = (BigDecimal)lin.get(2);

				// zero fordeles ikke
				if (opkrPrae.doubleValue() == 0.00)
					continue;

				// resten fordeler vi i uden og indenfor perioden
				BigDecimal[] praefordel = beloebsHelper.opdelPrAar(perioder, praemieFra, praemieTil, opkrPrae, 0);
				if (praefordel != null && praefordel.length > 1) {
					for (int six = 1; six < praefordel.length; six++) {
						// Bevidst fra index 1 - den første er fremtidige
						resultat = resultat.add(praefordel[six]);
					}
				}
			}
		}
		return resultat;
	}

	/**
	 * Dækningernes nettopræmie læses mest optimalt via aftalen<p>
	 * 
	 * @param pProdukt
	 * @param pDato, null = alle
	 */
	public NettoPraemiePrDaekning[] getNettoPraemiePrDaekning(Produkt pProdukt, BigDecimal pDato) {
		NettoPraemiePrDaekning[] alleNettoPraemiePrDaekning = getNettoPraemiePrDaekning(pDato);
		ArrayList<NettoPraemiePrDaekning> praemier = new ArrayList<NettoPraemiePrDaekning>();
		for (int i = 0; alleNettoPraemiePrDaekning != null && i < alleNettoPraemiePrDaekning.length; i++) {
			if (alleNettoPraemiePrDaekning[i].getProduktId().equals(pProdukt.getId()))
				praemier.add(alleNettoPraemiePrDaekning[i]);
		}
		if (pDato != null && praemier.size() > 1){
			// Det er åbenbart muligt med flere aktuelle præmier når fiktiv beregning??
			// LF: Ja, kendt fejl når flere utariferede ændringer på en forsikring / dækning
			Collections.sort(praemier, new ModelObjektGaeldendeComparator());
			while (praemier.size() > 1) {
				NettoPraemiePrDaekning nyeste = praemier.get(praemier.size() -1 );
				if (praemier.get(0).getGld().compareTo(nyeste.getGld()) < 0 ){
					praemier.remove(0);
				} else {
					throw new DatafejlException("Flere NettopræmiePrDaekning til samme dato: " + pProdukt.getId() + " dato: " + pDato);
				}
			}
		}
		return ContainerUtil.toArray(praemier);
	}

	/**
	 * @see Aftaletype#isAutoAdresseskifttilladt(Adressetype, BigDecimal)
	 */
	public boolean isAutoAdresseskifttilladt(Adressetype adressetype, BigDecimal dato) {

		return getAftaleTypen().isAutoAdresseskifttilladt(adressetype, dato);
	}

	// Metoder til at skaffe præmiebeløb m.m. -- Fiktive eller Faktiske
	public NettoPraemiePrDaekning eenNettoPraemiePrDaekning(BigDecimal pDato) {
		NettoPraemiePrDaekning[] nettoPraemiePrDaekningTabel = getNettoPraemiePrDaekning(pDato);
		if ((nettoPraemiePrDaekningTabel != null) && (nettoPraemiePrDaekningTabel.length > 0)) {
			for (int i = 0; i < nettoPraemiePrDaekningTabel.length; i++) {
				if (nettoPraemiePrDaekningTabel[i] != null) {
					return nettoPraemiePrDaekningTabel[i];
				}
			}
		}
		return null;
	}

	public BigDecimal findUdfoertBeregningsDato(BigDecimal pDato) {
		if (findesUdfoertBeregning(pDato)) {
			return eenNettoPraemiePrDaekning(pDato).getGld();
		}
		return null;
	}

	public boolean findesUdfoertBeregning(BigDecimal pDato) {
		if (eenNettoPraemiePrDaekning(pDato) != null) {
			return true;
		}
		return false;
	}

	public boolean isFiktivBeregning(BigDecimal pDato) {
		if (findesUdfoertBeregning(pDato)) {
			return eenNettoPraemiePrDaekning(pDato).isFiktivpraemiebrgn();
		}
		return true;
	}

	public AftaleOmkostningstypeIF[] findAftaleOmkostningstypeIF(BigDecimal pDato) {
		if (findesUdfoertBeregning(pDato)) {
			if (isFiktivBeregning(pDato)) {
				return getFiktivAftaleOmkostningstype(findUdfoertBeregningsDato(pDato)); // Pr.
				// BrgnDato
			}
			return getAftaleOmkostningstype(findUdfoertBeregningsDato(pDato)); // Pr.
			// BrgnDato
		}
		return null;
	}

	public ProduktOmkostningstypeIF[] findProduktOmkostningstypeIFIkkeStatsafgift(BigDecimal pDato) {
		ArrayList<ProduktOmkostningstypeIF> pdOmEjStat = new ArrayList<ProduktOmkostningstypeIF>();

		if (findesUdfoertBeregning(pDato)) {
			Produkt[] produktTabel = getDaekningerGld(pDato);
			for (int i = 0; ((produktTabel != null) && (i < produktTabel.length)); i++) {
				ProduktOmkostningstypeIF[] pdOmTabel = produktTabel[i].findProduktOmkostningstypeIFStatsafgift(pDato, false, null);
				for (int j = 0; ((pdOmTabel != null) && (j < pdOmTabel.length)); j++) {
					pdOmEjStat.add(pdOmTabel[j]);
				}
			}
			if ((pdOmEjStat != null) && (pdOmEjStat.size() > 0)) {
				return ContainerUtil.toArray(pdOmEjStat);
			}
			return null;
		}
		return null;
	}

	public Rabattype getRabattypeTotalkundeSenestetarifering(BigDecimal pDato) {
		AftaleRbtpPdIF[] aftaleRbtpPdIF = findAftaleRbtpPdIF(pDato);
		if (aftaleRbtpPdIF != null) {
			Set<Rabattype> rabattyperAlle = TotalkundetypeRegelManager.getRabattyperAlle();
			for (AftaleRbtpPdIF rel : aftaleRbtpPdIF) {
				Rabattype rabattype = rel.getRabattype();
				if (rel.getRabatbeloeb().doubleValue() == 0.00)
					continue;
				if (rabattyperAlle.contains(rabattype))
					return rabattype;
			}
		}
		return null;
	}
	public AftaleRbtpPdIF[] findAftaleRbtpPdIF(BigDecimal pDato) {
		NettoPraemiePrDaekning enBeregning = eenNettoPraemiePrDaekning(pDato);
		if (enBeregning != null) {
			if (enBeregning.isFiktivpraemiebrgn()) {
				return getFiktivAftaleRbtpPd(enBeregning.getGld()); // Pr. BrgnDato
			}
			return getAftaleRbtpPd(enBeregning.getGld()); // Pr. BrgnDato
		}
		return null;
	}

	public PdPraemieJusteringIF[] findPdPraemieJusteringIF(BigDecimal pDato) {
		ArrayList<PdPraemieJusteringIF> pdPrJust = new ArrayList<PdPraemieJusteringIF>();

		if (findesUdfoertBeregning(pDato)) {
			Produkt[] produktTabel = getDaekningerGld(pDato);
			for (int i = 0; ((produktTabel != null) && (i < produktTabel.length)); i++) {
				if (produktTabel[i].findPdPraemieJusteringIF(pDato) != null) {
					pdPrJust.add(produktTabel[i].findPdPraemieJusteringIF(pDato));
				}
			}
			if ((pdPrJust != null) && (pdPrJust.size() > 0)) {
				return ContainerUtil.toArray(pdPrJust);
			}
			return null;
		}
		return null;
	}

	public StormflodIF[] findStormflodIF(BigDecimal pDato) {
		if (findesUdfoertBeregning(pDato)) {
			if (isFiktivBeregning(pDato)) {
				return getFiktivStormflod(findUdfoertBeregningsDato(pDato)); // Pr. BrgnDato
			}
			return getStormflod(findUdfoertBeregningsDato(pDato)); // Pr. BrgnDato
		}
		return null;
	}

	public AftaleAarsAfgiftFordringIF[] findAftaleAarsAfgiftFordring(BigDecimal pDato) {
		if (findesUdfoertBeregning(pDato)) {
			if (isFiktivBeregning(pDato)) {
				return getFiktivAftaleAarsAfgift(pDato);
			}
			return getAftaleAarsAfgiftFordring(pDato);
		}
		return null;
	}

	public boolean udfoerGenberegningFiktivPraemieNoedvendig(BigDecimal pVisPraemieDato) {
		// Ønsket dato <= d.d.
		if (Periode.compare(pVisPraemieDato, false, Datobehandling.getDagsdatoBigD(), false) != Periode.BIGGER) {
			return false;
		}

		BigDecimal ikkeUdfForny = getIkkeUdfoertFornyelsesopgave();
		// Ingen Ikke-Udført-Fornyelse
		if (ikkeUdfForny == null) {
			return false;
		}

		// Ønsket dato < IkkeUdførtFornyelse
		if (Periode.compare(pVisPraemieDato, false, ikkeUdfForny, false) == Periode.SMALLER) {
			return false;
		}

		return true;
	}

	/**
	 * Specialudgave, der ubetinget udlægger en fiktiv tarifering pr. datoen ved førstkommende txcommit.
	 */
	public void udfoerGenberegningFiktivPraemieUbetinget(BigDecimal beregnDato) {
		DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer().addJob("GUIBFKTTR1", getId(),
				beregnDato, " ", this, 5);

	}
	public void udfoerGenberegningFiktivPraemieUbetingetMedrevidering(BigDecimal beregnDato) {
		udlaegRevidering(beregnDato);
		udfoerGenberegningFiktivPraemieUbetinget(beregnDato);

	}

	public void udfoerGenberegningFiktivPraemie(BigDecimal pVisPraemieDato) {
		BigDecimal ikkeUdfForny = getIkkeUdfoertFornyelsesopgave();

		// Ønsket dato = IkkeUdførtFornyelse
		if (Periode.compare(pVisPraemieDato, false, ikkeUdfForny, false) == Periode.EQUALS) {
			// Udfør pr. IkkeUdførtFornyelse
			udfoerFiktivberegningEenDato(ikkeUdfForny);
			refreshEfterBatch();
			return;
		}

		// Ingen fiktiv beregning for VisPræmieDato
		if (!findesUdfoertBeregning(pVisPraemieDato) || !isFiktivBeregning(pVisPraemieDato)) {
			// Udfør pr. IkkeUdførtFornyelse
			udfoerFiktivberegningEenDato(ikkeUdfForny);
			refreshEfterBatch();
			return;
		}

		BigDecimal fktBrgnDato = findUdfoertBeregningsDato(pVisPraemieDato);

		// Fiktiv beregning for VisPræmieDato = eller <> IkkeUdførtFornyelse
		if (Periode.compare(fktBrgnDato, false, ikkeUdfForny, false) != Periode.BIGGER) {
			// Udfør pr. IkkeUdførtFornyelse
			udfoerFiktivberegningEenDato(ikkeUdfForny);
			refreshEfterBatch();
			return;
		} else {
			// Udfør pr. Fiktiv-Beregnings-Gld
			udfoerFiktivberegningEenDato(fktBrgnDato);
			refreshEfterBatch();
			return;
		}
	}

	private void udfoerFiktivberegningEenDato(BigDecimal pFiktivBeregnDato) {
        AS400SubmitGUIbatchjob.changeToSynkronCall(true);
        boolean harSelvStartetTransaction = PersistensService.transactionBegin();
        DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer().addJob("GUIGBRGFK2", getId(),
                pFiktivBeregnDato, " ", this, 5);
        if (harSelvStartetTransaction){
            PersistensService.transactionCommit();
        }
        AS400SubmitGUIbatchjob.changeToSynkronCall(false);
	}

	/**
	 * @return gld på aftalens ikke-udførte fornyelsesopgave = næste fornyelsesdato. null hvis ingen opgave.
	 */
	public BigDecimal getIkkeUdfoertFornyelsesopgave() {
		DagbogAftale[] dagbogAftaleTab = getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), false, null, 0);
		for (int i = 0; (dagbogAftaleTab != null) && (i < dagbogAftaleTab.length); i++) {
			// if (!dagbogAftaleTab[i].isUdfoert() && dagbogAftaleTab[i].getOpgavetype().isFornyelsesopgave()) {
			return dagbogAftaleTab[i].getGld();
			// }
		}

		return null;
	}

	/**
	 * @return gld på aftalens ikke-udførte bonusreguleringsopgave = næste bonusreguleringssdato. null hvis ingen opgave.
	 */
	public BigDecimal getIkkeUdfoertBonusreguleringsopgave() {
		DagbogAftale[] dagbogAftaleTab = getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), false, null, 0);
		for (int i = 0; (dagbogAftaleTab != null) && (i < dagbogAftaleTab.length); i++) {
			return dagbogAftaleTab[i].getGld();
		}

		return null;
	}

	/**
	 * @return gld på aftalens ikke-udførte fornyelsesopgave = næste fornyelsesdato. null hvis ingen opgave.
	 */
	public DagbogAftale getIkkeUdfoertFornyelsesDelopkraevningsopgaveObject() {
		DagbogAftale[] dagbogAftaleTab = getDagbogAftale(
				new Opgavetype[] {OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), OpgavetypeImpl.getOpgavetype(Opgavetype.DELOPK) }, 
				false, null, 0);
		if (dagbogAftaleTab != null && dagbogAftaleTab.length > 1){
			Arrays.sort(dagbogAftaleTab, new ModelObjektGaeldendeComparator());
		}
		for (int i = 0; (dagbogAftaleTab != null) && (i < dagbogAftaleTab.length);) {
			// if (!dagbogAftaleTab[i].isUdfoert() && dagbogAftaleTab[i].getOpgavetype().isFornyelsesopgave()) {
			
			return dagbogAftaleTab[i];
			// }
		}

		return null;
	}
	public DagbogAftale getIkkeUdfoertFornyelsesOpgaveObject() {
		return getIkkeUdfoertOpgaveObject(Opgavetype.FORNYELSE);
	}
	public DagbogAftale getIkkeUdfoertDelopkraevningsOpgaveObject() {
		return getIkkeUdfoertOpgaveObject(Opgavetype.DELOPK);
	}
	public DagbogAftale getIkkeUdfoertOpgaveObject(String pOpgtp) {
		DagbogAftale[] dagbogAftaleTab = getDagbogAftale(OpgavetypeImpl.getOpgavetype(pOpgtp), false, null, 1);
		if (dagbogAftaleTab != null && dagbogAftaleTab.length > 0) {
			if (dagbogAftaleTab[0].isUdfoert())
				throw new IllegalStateException("Denne metode må ikke returnere udført dagbogaftale");
			return dagbogAftaleTab[0];
		}
		return null;
	}
	/**
	 * @return gld på aftalens ikke-udførte delopkrævningsopgave null hvis ingen opgave.
	 */
	public BigDecimal getIkkeUdfoertDelopkraevningsopgave() {
		DagbogAftale[] dagbogAftaleTab = getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.DELOPK), false, null, 0);
		for (int i = 0; (dagbogAftaleTab != null) && (i < dagbogAftaleTab.length); i++) {
			// if (!dagbogAftaleTab[i].isUdfoert() && dagbogAftaleTab[i].getOpgavetype().isDelopkraevningsopgave()) {
			return dagbogAftaleTab[i].getGld();
			// }
		}

		return null;
	}

	/**
	 * 
	 * @return alle DagbogAftale på aftalen - uanset opgavetyper, udførte eller ikke-udførte
	 */
	public DagbogAftale[] getAlleDagbogAftale()  {
	    return (DagbogAftale[]) DBServer.getInstance().getVbsf().get(this, DagbogAftale.DAGBOGAFTALE);
	  }

	/**
	 * @see #getDagbogAftale(Opgavetype, Boolean, BigDecimal, int)
	 * @see #getDagbogAftale(Opgavetype[], Boolean, BigDecimal, int)
	 */
	public DagbogAftale[] getDagbogAftale(Opgavetype pOpgavetype, BigDecimal pGld, int pMaxCount) {
		return getDagbogAftale(pOpgavetype, null, pGld, pMaxCount);
	}

	/**
	 * @see #getDagbogAftale(Opgavetype, BigDecimal, int)
	 * @see #getDagbogAftale(Opgavetype[], Boolean, BigDecimal, int)
	 */
	public DagbogAftale[] getDagbogAftale(Opgavetype pOpgavetype, Boolean pKunUdfoerte, BigDecimal pGld, int pMaxCount) {
		Opgavetype[] o = new Opgavetype[1];
		o[0] = pOpgavetype;
		return getDagbogAftale(o, pKunUdfoerte, pGld, pMaxCount);
	}

	/**
	 * Get alle DagbogAftale af de givne Opgavetyper, udførte eller ikke-udførte
	 * 
	 * @param pOpgavetype
	 *            array , null = alle
	 * @param pKunUdfoerte
	 *            null=alle, true=kun udførte opgaver, false=kun ikke-udførte
	 * @see #getDagbogAftale(Opgavetype, BigDecimal, int)
	 * @see #getDagbogAftale(Opgavetype, Boolean, BigDecimal, int)
	 */
	public DagbogAftale[] getDagbogAftale(Opgavetype[] pOpgavetype, Boolean pKunUdfoerte, BigDecimal pGld, int pMaxCount) {
		OQuery qry = DBServer.getInstance().getVbsf().queryCreateTom();

		for (int i = 0;pOpgavetype != null && i < pOpgavetype.length; i++){
			Opgavetype opgtp = pOpgavetype[i];
			int pt = OQuery.NO_PAR;
			if (pOpgavetype.length > 1){
				// Hvis flere opgavetype wrapper vi n OR-clauses i venstre- og højreparantes.
				if (i == 0)
					pt = OQuery.BEG_PAR;
				if (i == (pOpgavetype.length - 1))
					pt = OQuery.END_PAR;
			}
			qry.add(opgtp.getId(), "Opgavetype", OQuery.EQUAL, OQuery.OR, pt);
			// OBS : må ikke laves om til SQL-IN, da det ikke er understøttet af in-mem-queries.
		}

		if (pKunUdfoerte != null) {
			qry.add(GaiaConst.NULBD, "udfoertden", (pKunUdfoerte.booleanValue() ? OQuery.NOT_EQUAL : OQuery.EQUAL), OQuery.AND);
		}
		// else alle

		if (pGld != null && pGld.intValue() != 0) {
			qry.add(pGld, "gld", OQuery.EQUAL, OQuery.AND);
		}

		if (pMaxCount > 0) {
			qry.addOrder("gld", OQuery.DESC);
			qry.setMaxCount(pMaxCount);

		}

		DagbogAftale[] dgaf = (DagbogAftale[]) DBServer.getInstance().getVbsf().get(this, DagbogAftale.DAGBOGAFTALE, qry);
		return dgaf;
	}

	/**
	 * Get alle DagbogAftale som enten er en fornyelses opgave eller en delopkrævnings opgave.
	 *
	 * @param pOpgavedato
	 * @param pKunUdfoerte null=alle, true=kun udførte opgaver, false=kun ikke-udførte
	 */
	public DagbogAftale[] getDagbogAftaleFornyelseEllerDelopkraevning(BigDecimal pOpgavedato, Boolean pKunUdfoerte) {
		return getDagbogAftale(OpgavetypeImpl.getOpgavetyperFornyelseOgDelpokraevning(), pKunUdfoerte, pOpgavedato, 0);
	}

	public DagbogAftale getFoerstKommendeDagbogAftale(Opgavetype pOpgavetype, Boolean pKunUdfoerte, BigDecimal pDato) {
		if (pOpgavetype != null && pDato != null) {
			OQuery qry = DBServer.getInstance().getVbsf().queryCreateTom();
			qry.add(pOpgavetype.getId(), "Opgavetype", OQuery.EQUAL);

			if (pKunUdfoerte != null) {
				qry.add(GaiaConst.NULBD, "udfoertden", (pKunUdfoerte.booleanValue() ? OQuery.NOT_EQUAL : OQuery.EQUAL));
			}

			qry.add(pDato, "gld", OQuery.GREATER_OR_EQUAL);

			qry.addOrder("gld", OQuery.ASC);
			qry.setMaxCount(1);

			DagbogAftale[] dgaf = (DagbogAftale[]) DBServer.getInstance().getVbsf().get(this, DagbogAftale.DAGBOGAFTALE, qry);
			if (dgaf != null && dgaf.length > 0) {
				return dgaf[0];
			}
		}
		return null;
	}

	public DagbogAftale getSenesteDagbogAftaleFoer(Opgavetype pOpgavetype, Boolean pKunUdfoerte, BigDecimal pDato) {
		if (pOpgavetype != null && pDato != null) {
			OQuery qry = DBServer.getInstance().getVbsf().queryCreateTom();
			qry.add(pOpgavetype.getId(), "Opgavetype", OQuery.EQUAL);

			if (pKunUdfoerte != null) {
				qry.add(GaiaConst.NULBD, "udfoertden", (pKunUdfoerte.booleanValue() ? OQuery.NOT_EQUAL : OQuery.EQUAL));
			}

			qry.add(pDato, "gld", OQuery.LESS);

			qry.addOrder("gld", OQuery.DESC);
			qry.setMaxCount(1);

			DagbogAftale[] dgaf = (DagbogAftale[]) DBServer.getInstance().getVbsf().get(this, DagbogAftale.DAGBOGAFTALE, qry);
			if (dgaf != null && dgaf.length > 0) {
				return dgaf[0];
			}
		}
		return null;
	}

	/**
	 * Adder et job til afvikling af statuskørsel på aftalen
	 * 
	 * @param pMedGiroUdskrift
	 * @param pMedOpkraevningsgebyr
     * @param pTarifDato
     * @param pPrintJobId
     * @param asSamlAlt
     *
	 */
	private String bestilStatusKoerselDanBatchParm(boolean pMedGiroUdskrift, boolean pMedOpkraevningsgebyr, BigDecimal pTarifDato,
                                                   String pPrintJobId, boolean asSamlAlt) {
		String batchParm = "NN"; // Pos.1 GiroUdskrift -- Pos.2 Opkrævningsgebyr -- (Ja/Nej = Med/Uden)

		if (pMedGiroUdskrift || asSamlAlt) {
		    String pos1 = pMedGiroUdskrift ? "J" : "N";
		    String pos2 = pMedOpkraevningsgebyr ? "J" : "N";
		    batchParm = pos1 + pos2;
		}
		if (getStatusKoerselMaxAfvDage()) {	
			batchParm = batchParm + "J";
			batchParm = batchParm + "   ";//betyder at antal afviklingsdage er 999!
			setStatusKoerselMaxAfvDage(false);
		} else {
			// OBS 117A - skru afviklingsdage frem således at alle dagbogsopgaver udføres til og med den udførte tarifering der ligger 
			// ud over afviklingsdage
			BigDecimal afviklingsDageSkruFrem = null;
			BigDecimal senesteUdfoerteTarifering = this.getSenesteUdfoerteTariferingsOpgave();
			if(pTarifDato != null && (senesteUdfoerteTarifering == null || 
					pTarifDato.compareTo(senesteUdfoerteTarifering) > 0)) {
				senesteUdfoerteTarifering = pTarifDato;
			}
			if(senesteUdfoerteTarifering!=null) {
				int afviklingsDageRegel = DBServer.getInstance().getSelskabsOplysning().getAntAfviklingsdage();
				BigDecimal dd = Datobehandling.getDagsEllerDriftsDato();
				BigDecimal datoForAfviklingsDage = Datobehandling.datoPlusMinusAntal(dd, afviklingsDageRegel);
            	if(senesteUdfoerteTarifering.compareTo(datoForAfviklingsDage) > 0){
            		afviklingsDageSkruFrem = new BigDecimal(Datobehandling.antalDageIPeriodenAltidPositiv(dd, senesteUdfoerteTarifering));
            		afviklingsDageSkruFrem = afviklingsDageSkruFrem.add(new BigDecimal(-1));
            		if(afviklingsDageSkruFrem.compareTo(new BigDecimal(999)) >= 0) {
            			batchParm = batchParm + "J";
            		}
            		else {
            			batchParm = batchParm + "X";
            			// her sikrer vi os at stringDato altid er længden 3.
                		if(afviklingsDageSkruFrem.compareTo(new BigDecimal(10)) < 0) {
                    		batchParm = batchParm + "0";
                		}
                		if(afviklingsDageSkruFrem.compareTo(new BigDecimal(100)) < 0) {
                    		batchParm = batchParm + "0";
                		}
                		String afviklingsDageSkruFremString = afviklingsDageSkruFrem.toString().trim();
                		batchParm = batchParm + afviklingsDageSkruFremString;
            		}
            	}
			}
			else {
				batchParm = batchParm + "N";
			}
		}

		//printJobId
		if (pPrintJobId == null || pPrintJobId.equals(GaiaConst.TOMSTRING)){
			pPrintJobId = "       ";
		}
		batchParm = batchParm + pPrintJobId;

		return batchParm;
	}
	/**
	 * Adder et job til afvikling af statuskørsel på aftalen
	 * 
	 * @param pMedGiroUdskrift
	 * @param pMedOpkraevningsgebyr
	 * @param pTarifDato Tariferer tarifopgaver frem til og med denne dato.
	 */
	public void bestilStatusKoersel(boolean pMedGiroUdskrift, boolean pMedOpkraevningsgebyr, BigDecimal pTarifDato) {
	   bestilStatusKoersel(pMedGiroUdskrift, pMedOpkraevningsgebyr, pTarifDato, false);
    }
    /**
     * Adder et job til afvikling af statuskørsel på aftalen
     *
     * @param pMedGiroUdskrift
     * @param pMedOpkraevningsgebyr
     * @param pTarifDato Tariferer tarifopgaver frem til og med denne dato.
     * @param asBehandlKunde  hvis true udføres der statuskørsel som i natdrift, bl.a. mere omfattende samlAfregninger
     *
     */
    public void bestilStatusKoersel(boolean pMedGiroUdskrift, boolean pMedOpkraevningsgebyr, BigDecimal pTarifDato, boolean asBehandlKunde) {
		TraceUtil.newInstance().debugPoint(log_, this, this.getId(), pMedGiroUdskrift, pMedOpkraevningsgebyr, pTarifDato, this.isDoedTilbud());
		//Transform
		String printJobId = null;
		Individ individ = this.getTegnesAfIndivid();
		if(dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.IS_RUNNING_TRANSFORM.isPresent() && GensamUtil.isRunningOnline() && !this.isRykkerKoerselIgang_){
			PrintjobPersistensService printjobPersistensService = new PrintjobPersistensService(individ, "bestilStatusKoersel - AftaleId: " + this.getId(), pTarifDato);
			printJobId = printjobPersistensService.getPrintjobId();
		}
		
		String batchParm = this.bestilStatusKoerselDanBatchParm(pMedGiroUdskrift, pMedOpkraevningsgebyr, pTarifDato, printJobId, asBehandlKunde);
		String pgmnavn = asBehandlKunde ? AS400SubmitGUIbatchjob.PGM_STATUSKOERSEL_ASBEHANDLKUNDE : AS400SubmitGUIbatchjob.PGM_STATUSKOERSEL;
		DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer().addJob(pgmnavn, this.getId(), BigDecimal.ZERO, batchParm,
				this, 7);

	}

	/**
	 * Adder et job til afvikling af statuskørsel på aftalen
	 * 
	 * @param pMedGiroUdskrift
	 * @param pMedOpkraevningsgebyr
	 */
	public void bestilStatusKoersel(boolean pMedGiroUdskrift, boolean pMedOpkraevningsgebyr) {
		bestilStatusKoersel(pMedGiroUdskrift, pMedOpkraevningsgebyr, null);
	}

	public void bestilPoliceUdskrift(BigDecimal pDato) {
		String batchParm = " ";
		DBServer.getInstanceSave().getVbsf().getAS400SubmitGUIbatchjobBuffer()
				.addJob("GUIBPOLUD1", this.getId(), pDato, batchParm, this, 7);
	}
	
	/** Finder alle fremtidige minimumspræmier i forhold til medsendte dato. */
	public AftaleMinPrae[] getFremtidigeAftaleMinPrae(BigDecimal pDato) {
		AftaleMinPrae[] alleMinPrae = this.getAftaleMinPrae();

		if (alleMinPrae != null && alleMinPrae.length > 0) {
			AftaleMinPrae[] fremtidigeMinPrae = (AftaleMinPrae[]) Datobehandling.findFremtidige(alleMinPrae, pDato);
			return fremtidigeMinPrae;
		}
		return null;
	}

	// genererede accessmetoder til AftaleMinPrae
	public AftaleMinPrae[] getAftaleMinPrae() {
		return (AftaleMinPrae[]) DBServer.getInstance().getVbsf().get(this, "AftaleMinPrae");
	}

	public AftaleMinPrae[] getAftaleMinPrae(BigDecimal pDato) {
		return (AftaleMinPrae[]) DBServer.getInstance().getVbsf().get(this, "AftaleMinPrae", pDato);
	}

	public void addAftaleMinPrae(AftaleMinPrae pAftaleMinPrae) {
		PersistensService.addToCollection(this, "AftaleMinPrae", pAftaleMinPrae);
	}

	public void removeAftaleMinPrae(AftaleMinPrae oldAftaleMinPrae) {
		PersistensService.removeFromCollection(this, "AftaleMinPrae", oldAftaleMinPrae);
	}

	public AftaleMinPrae getAftaleMinPraeMedHoejesteBeloeb(BigDecimal pDato) {
		AftaleMinPrae[] aftaleMinP = getAftaleMinPrae(pDato);
		if (aftaleMinP != null && aftaleMinP.length > 0) {
			if (aftaleMinP.length == 1) {
				return aftaleMinP[0];
			}
			// Hvis der er fundet flere, find den med højeste beløb
			else if (aftaleMinP.length > 1) {
				AftaleMinPrae max = aftaleMinP[0];
				for (int k = 0; k < aftaleMinP.length; k++) {
					if (aftaleMinP[k].getBeloeb().compareTo(max.getBeloeb()) == 1) {
						max = aftaleMinP[k];
					}
				}
				return max;
			}
		}
		return null;
	}

	private static final String[] cols = { "NettoPraemiePrDaekning", Produkt.PRODUKT, "AftaleStatustype", "Stormflod", "FiktivStormflod",
			"AftaleOmkostningstype", "FiktivAftaleOmkostningstype", AftaleYdtpAng.AftaleYdtpAng, GenstandYdtpAng.GenstandYdtpAng, ProduktYdtpAng.ProduktYdtpAng,
			"FiktivAftaleRbtpPd", "DagbogAftale", "AftaleRbtpPd", "AfregningFritekstHoved", "Genstand", "AftaleFlytningLogFraAftale",
			AftaleTotalkundetype.AFTALETOTALKUNDETYPE, "Provision", "ForeloebigProvision", "RrTOrAf", "AftaleRegDatoAfregning", 
			"AftaleMinPrae", "IndividAftale", "Aftalehaendelse", "ForsikringstagerAftalehaendelseProvisionmodtager",
			"AftaleOmraade", "AftaleAfegn", "OverblikHaendelseManuel", "AftaleAarsAfgiftFordring", "FiktivAftaleAarsAfgift"};

	public void refreshEfterBatchMedTjekAfTransaction() {
		boolean openTransaction = DBServer.getInstance().getVbsf().isTransactionOpen();
		if (openTransaction) {
			PersistensService.transactionCommit();
		}

		refreshEfterBatch();

		if (openTransaction) {
			PersistensService.transactionBegin();
		}
	}

	public void refreshEfterBatch() {
	    log_.info("Start refreshEfterBatch for aftale " + getId() + " hash " + hashCode());
		DBServer.getVbsfInst().refresh(this);
	    log_.info("Refresh for aftale " + getId() + " hash " + hashCode());
		DBServer.getInstance().getVbsf().markCollectionDirty(this, cols);
	    log_.info("Refresh for aftalecolls " + getId() + " hash " + hashCode());

        final Genstand[] genstande = this.getGenstand();
        if (genstande != null) {
            for (Genstand genstand : genstande){
                DBServer.getInstance().getVbsf().markCollectionDirty((AbstractModelObjekt) genstand, GenstandYdtpAng.GenstandYdtpAng);
            }
        }

        Produkt[] daekninger = this.getDaekninger();
		if (daekninger != null) {
			for (int i = 0; i < daekninger.length; i++) {
				daekninger[i].refreshEfterBatch();
			}
		}

		DBServer.getInstance().getVbsf().markCollectionDirty(getTegnesAfIndivid(), "OverblikHaendelseManuel");
		if (TotalkundetypeRegelManager.isTotalkundeSelskab()) { // todo pk revurder denne if - hvorfor skal det gøres så omfattende?
			DBServer.getInstance().getVbsf().discardAll(TotalkundetypeIndividImpl.class); // aht. evt. oqueries
			DBServer.getInstance().getVbsf().markCollectionDirty(getTegnesAfIndivid(), TotalkundetypeIndivid.TOTALKUNDETYPEINDIVID);
		}
		// OBS E756
		// #12473 Statuskørslen kan opdatere afregninger, derfor skal vi have dem reloaded
	    log_.info("Refresh med uheldige discardAll " + getId() + " hash " + hashCode());
		DBServer.getInstance().getVbsf().discardAll(AfregningImpl.class);
		DBServer.getInstance().getVbsf().discardAll(FordringImpl.class);
		DBServer.getInstance().getVbsf().discardAll(GebyrImpl.class);
		DBServer.getInstance().getVbsf().discardAll(RrTOrImpl.class);
		DBServer.getInstance().getVbsf().discardAll(RrTOrFhImpl.class);
		DBServer.getInstance().getVbsf().discardAll(RrTOrAfImpl.class);
		DBServer.getInstance().getVbsf().discardAll(RrTOrPdImpl.class);
		DBServer.getInstance().getVbsf().discardAll(AftaleRegDatoAfregningImpl.class);
		DBServer.getInstance().getVbsf().discardAll(AfregningFritekstHovedImpl.class);
		DBServer.getInstance().getVbsf().discardAll(ProduktYdtpAngInclAfspejlingerImpl.class); // pk overvejer en bedre løsning
		DBServer.getInstance().getVbsf().discardAll(ReasBeregnRegisterImpl.class);
		DBServer.getInstance().getVbsf().discardAll(ReasFordringWorkImpl.class);
		DBServer.getInstance().getVbsf().discardAll(ReasProduktOphoertImpl.class);
        DBServer.getInstance().getVbsf().discardAll(GsproMapningImpl.class);
		DBServer.getInstance().getVbsf().discardAll(OverblikHaendelseManuelImpl.class);
	    log_.info("Slut refreshEfterBatch for aftale " + getId() + " hash " + hashCode());
    }

	// genererede accessmetoder til AftaleEgenbetaltOmkost
	public AftaleEgenbetaltOmkost[] getAftaleEgenbetaltOmkost() {
		return (AftaleEgenbetaltOmkost[]) DBServer.getInstance().getVbsf().get(this, "AftaleEgenbetaltOmkost");
	}

	public void addAftaleEgenbetaltOmkost(AftaleEgenbetaltOmkost pAftaleEgenbetaltOmkost) {
		PersistensService.addToCollection(this, "AftaleEgenbetaltOmkost", pAftaleEgenbetaltOmkost);
	}

	public void removeAftaleEgenbetaltOmkost(AftaleEgenbetaltOmkost oldAftaleEgenbetaltOmkost) {
		PersistensService.removeFromCollection(this, "AftaleEgenbetaltOmkost", oldAftaleEgenbetaltOmkost);
	}

	public String getModulLockTekst() {
		return getTabelnavn();
	}

	public String getModulLockRefreskIndividId() {
		return getTegnesAfIndividId();
	}

	/**
	 * Undersøger DagbogProdukt for tidligste ikke-udførte tariferingsopgave, og returnere dennes opgave gld. 
	 * <br>
	 * Metoden anvender named SQL som kan findes i VBSF schemaet under SQL Commands-> TidligsteIkkeUdfoerteProduktDagbogDato
	 * 
	 * @return MIN(DGPD.GLDDAT) på ej-udførte Dagbogprodukt eller null hvis ingen opgave
	 */
	public BigDecimal getTidligsteIkkeUdfoerteTariferingsOpgave() {
		RegelsaetType opgtp = QueryService.lookupRegelType(OpgavetypeImpl.class, Opgavetype.TARIFERING);
		String[] parm = { opgtp.getId(), getAftaleId() };
		BigDecimal gld = QueryService.getValueSingle("TidligsteIkkeUdfoerteProduktDagbogDato", ContainerUtil.asList(parm));
		if (gld != null && gld.intValue() > 0)
			return gld;
		return null;
	}
	
	/**
	 * Undersøger DagbogProdukt for seneste udførte tariferingsopgave, og returnere dennes opgave gld. 
	 * <br>
	 * Metoden anvender named SQL som kan findes i VBSF schemaet under SQL Commands-> SenesteUdfoerteProduktDagbogDato
	 * 
	 * @return MAX(DagbogProdukt.gld) på alle aftalens dækninger med udført tariferingsopgave, eller null hvis ingen
	 */
	public BigDecimal getSenesteUdfoerteTariferingsOpgave() {
		RegelsaetType opgtp = QueryService.lookupRegelType(OpgavetypeImpl.class, Opgavetype.TARIFERING);
		String[] parm = { opgtp.getId(), getAftaleId() };
		return QueryService.getValueSingle("SenesteUdfoerteProduktDagbogDato", ContainerUtil.asList(parm));

	}
	
	/**
	 * @return alle dagbog produkt til tarifopgaver pr. en bestemt dato.
	 */
	public DagbogProdukt[] getDagbogProduktTarifopgave(BigDecimal pDato) {
		return getDagbogProdukt(OpgavetypeImpl.getOpgavetype(Opgavetype.TARIFERING), pDato);
	}
	/**
	 * @return alle dagbog produkt til en opgavetype pr. en bestemt dato.
	 */
	public DagbogProdukt[] getDagbogProdukt(Opgavetype pOpgavetype, BigDecimal pDato) {
		return getDagbogProdukt(pOpgavetype, pDato, OQuery.EQUAL, null);
	}

	/**
	 * @return alle dagbog produkt til en opgavetype pr. en bestemt dato.
	 */
	public DagbogProdukt[] getDagbogProdukt(Opgavetype pOpgavetype, BigDecimal pDato, int datoComp, Boolean udfoerte) {
		String clsProdukt = ProduktImpl.class.getName();
		OQuery qry = QueryService.queryCreate(DagbogProduktImpl.class);
		qry.add(pOpgavetype.getId(), "Opgavetype");
		if (pDato != null)
			qry.add(pDato, "gld", datoComp);
		if (udfoerte != null)
			qry.add(BigDecimal.ZERO, "udfdato", (udfoerte ? OQuery.GREATER : OQuery.EQUAL));
		qry.add(getId(), clsProdukt + ".Aftale");
		qry.addJoin(qry.getClassName() + ".Produkt", clsProdukt + ".produktId");
		return (DagbogProdukt[]) DBServer.getVbsfInst().queryExecute(qry);
	}

	/**
	 * Undesøger DagbogAftale for seneste udført fornyelse eller delopkrævning og returnere opgavens gld. Metoden
	 * anvender named SQL som kan findes i VBSF schemaet under SQL Commands -> SenesteUdfoertAftaleDagbogDato.
	 * @return MAX(DGAF.GLDDAT) af de 2 givne typer, eller null hvis ingen
	 */
	public BigDecimal getSenesteUdfoertFornyelseEllerDelopkraevning() {
		String[] parm = { getAftaleId(), "FORNYELSE", "DELOPK" }; // Bemærk parameter rækkefølge (rækkefølgen af 2 og
		// 3 indbyrdes er dog ikke kritisk pga. samme
		// type/anvendelse)
		return QueryService.getValueSingle("SenesteUdfoertAftaleDagbogDato", ContainerUtil.asList(parm));
	}

	/**
	 * Undesøger DagbogAftale for tidligste ikke-udført fornyelse eller delopkrævning og returnerer opgavens gld.
	 * Metoden anvender named SQL som kan findes i VBSF schemaet under SQL Commands ->
	 * TidligsteIkkeUdfoertAftaleDagbogDato.
	 * @return MIN(DGAF.GLDDAT) på ikke-udførte opgaver af den givne type, eller null hvis ingen
	 */
	public BigDecimal getTidligsteIkkeUdfoertFornyelseEllerDelopkraevning() {
		String[] parm = { getAftaleId(), "FORNYELSE", "DELOPK" }; // Bemærk parameter rækkefølge (rækkefølgen af 2 og
		// 3 indbyrdes er dog ikke kritisk pga. samme
		// type/anvendelse)
		BigDecimal gld = QueryService.getValueSingle("TidligsteIkkeUdfoertAftaleDagbogDato", ContainerUtil.asList(parm));
		if (gld != null && gld.intValue() > 0)
			return gld;
		return null;
		
	}

	/**
	 * @return seneste faktisk udførte fornyelsesdato (fornyelsesopgavens gældende) eller null hvis aldrig fornyet
	 * 
	 */
	public BigDecimal getSenesteUdfoertFornyelsesdato() {
		DagbogAftale[] dagbogAf = this.getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), true, null, 1);
		for (int i = 0; dagbogAf != null && i < dagbogAf.length; i++) {
			return dagbogAf[i].getGld();
		}
		return null;
	}

	/**
	 * 
	 * @return seneste faktisk udførte fornyelsesdato i forhold til dato parameter, eller null hvis aldrig fornyet. Dvs.
	 *         funktionen kigger på alle udførte fornyelser, og returnerer den sidste udførte før dato parameter.
	 * @param dato
	 */
	public BigDecimal getSenesteUdfoertFornyelsesdato(BigDecimal dato) {
		DagbogAftale[] dagbogAf = this.getDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.FORNYELSE), true, null, 0);
		DagbogAftale dagbogAftale = null;
		for (int i = 0; dagbogAf != null && i < dagbogAf.length; i++) {
			DagbogAftale dagbogAftaleTemp = dagbogAf[i];
			// find task with latest date:
			// if task to return not set or current task has later date then
			// task to return, and current taks´s date is before passed date
			// set task to return = current task
			if ((dagbogAftale == null || dagbogAftale.getGld().compareTo(dagbogAftaleTemp.getGld()) == -1)
					&& dagbogAftaleTemp.getGld().compareTo(dato) == -1) {
				dagbogAftale = dagbogAftaleTemp;
			}
		}
		return dagbogAftale != null ? dagbogAftale.getGld() : null;
	}
	
	/**
	 * Kalde metoden getSenesteUdfoertFornyelsesdato(BigDecimal dato) med den lille krølle, 
	 * at vi lægger en dag til således, at fornyelser udført på parameter dato også omfattes.
	 * 
	 * @param pDato
	 * @return
	 * @since 20/06/2008 Gensafe Pro 2.0
	 */
	public BigDecimal getSenesteUdfoertFornyelsesdatoInkl(BigDecimal pDato) {
		return getSenesteUdfoertFornyelsesdato(Datobehandling.datoPlusMinusAntal(pDato, 1));
	}

	@Override
	public boolean findesUdfoertForsikringsReguleringsOpgaver(BigDecimal pOpgavedato) {
        DagbogAftale[] dagbogAf = this.getDagbogAftale(OpgavetypeImpl.getOpgavetyperRegulering(), true, pOpgavedato, 1);
        if (dagbogAf != null) {
            return true;
        }
		return false;
	}

	/**
	 * @return array af udførte dagbogsopgaver der er sorteret i stigende orden efter gældende. <code>null</code> hvis
	 *         ingen.
	 * @since 30/06/2006 Gensafe Pro 1.6
	 */
	public DagbogAftale[] getUdfoertForsikringsReguleringsOpgaver() {
        DagbogAftale[] result = getDagbogAftale(OpgavetypeImpl.getOpgavetyperRegulering(), true, null, 0);
        if (result != null) {
            Arrays.sort(result, new ModelObjektGaeldendeComparator());
        }
        return result;
	}

	/**
	 * Undersøger om der findes <B>udført</B> fornyelsesopgave
	 * <P>
	 * 
	 * @return true hvis der findes udførte opgaver som er del af en fornyelse, og opgavens gld er forskellig fra
	 *         aftalens gld.
	 */
	public boolean findesUdfoertFornyelseOpgave() {
        DagbogAftale[] dagbogAf = this.getDagbogAftale(OpgavetypeImpl.getOpgavetyperDelAfFornyelse(), true, null, 0);
        if (dagbogAf != null) {
            for (int k = 0; k < dagbogAf.length; k++) {
                if (dagbogAf[k].getOpgavetype().isFornyelsesopgave()){
                // Skal kun returnere true hvis opgavens gld er forskellig fra aftalens gld
                if (dagbogAf[k].getGld().compareTo(this.getGld()) != 0)
                    return true;
                }
            }
        }
		return false;
	}

	public int getIdLabelConst() {
		return AS400DanIdentifikationsLabel.AFTALE;
	}

	public void refreshLabelCollections() {
		refreshXOAftaleLabel();
	}

	/**
	 * Dato sammenligning foregår på {@link AfregningFritekstHoved#getTariferingsdato()}.
	 * 
	 * @return <code>null</code> hvis der ikke findes nogen afregningsfritekst hoved relationer tilknyttet på denne
	 *         dato.
	 */
	public AfregningFritekstHoved[] getAfregningFritekstHoved(BigDecimal dato) {

		AfregningFritekstHoved[] temp = getAfregningFritekstHoved();
		List<AfregningFritekstHoved> result = new ArrayList<AfregningFritekstHoved>((temp == null ? 0 : temp.length));

		for (int i = 0; temp != null && i < temp.length; i++) {

			if (temp[i].getTariferingsdato().equals(dato)) {
				result.add(temp[i]);
			}
		}

		return ContainerUtil.toArray(result);
	}

	public List<AfregningFritekstHoved> getAfregningFritekstHovedTilOgMedDato(BigDecimal dato) {

		AfregningFritekstHoved[] temp = getAfregningFritekstHoved();
		List<AfregningFritekstHoved> result = new ArrayList<AfregningFritekstHoved>((temp == null ? 0 : temp.length));

		for (int i = 0; temp != null && i < temp.length; i++) {

			if (temp[i].getTariferingsdato().compareTo(dato) <= 0) {
				result.add(temp[i]);
			}
		}

		return result;
	}

	public AfregningFritekstHoved[] getAfregningFritekstHoved() {
		return (AfregningFritekstHoved[]) DBServer.getInstance().getVbsf().get(this, "AfregningFritekstHoved");
	}

	public void addAfregningFritekstHoved(AfregningFritekstHoved pAfregningFritekstHoved) {
		PersistensService.addToCollection(this, "AfregningFritekstHoved", pAfregningFritekstHoved);
	}

	public void removeAfregningFritekstHoved(AfregningFritekstHoved oldAfregningFritekstHoved) {
		PersistensService.removeFromCollection(this, "AfregningFritekstHoved", oldAfregningFritekstHoved);
	}

	// genererede accessmetoder til ReasBeregnRegister
	public ReasBeregnRegister[] getReasBeregnRegister() {
		return (ReasBeregnRegister[]) DBServer.getInstance().getVbsf().get(this, "ReasBeregnRegister");
	}

	public void addReasBeregnRegister(ReasBeregnRegister pReasBeregnRegister) {
		PersistensService.addToCollection(this, "ReasBeregnRegister", pReasBeregnRegister);
	}

	public void removeReasBeregnRegister(ReasBeregnRegister oldReasBeregnRegister) {
		PersistensService.removeFromCollection(this, "ReasBeregnRegister", oldReasBeregnRegister);
	}

	public AftaleGenoptagLog[] getAftaleGenoptagLogFraAftale() {
		return (AftaleGenoptagLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleGenoptagLogFraAftale");
	}

	public AftaleGenoptagLog[] getAftaleGenoptagLogTilAftale() {
		return (AftaleGenoptagLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleGenoptagLogTilAftale");
	}

	public void addAftaleGenoptagLogTilAftale(AftaleGenoptagLog pAftaleGenoptagLogTilAftale) {
		PersistensService.addToCollection(this, "AftaleGenoptagLogTilAftale", pAftaleGenoptagLogTilAftale);
	}

	public void addAftaleGenoptagLogFraAftale(AftaleGenoptagLog pAftaleGenoptagLogFraAftale) {
		PersistensService.addToCollection(this, "AftaleGenoptagLogFraAftale", pAftaleGenoptagLogFraAftale);
	}

	/**
	 * Undersøger om aftalen tidligere er genoptaget til en anden.
	 * 
	 * @return boolean true hvis aftalen tidligere er genoptaget.
	 */
	public boolean isGenoptagetFraAftale() {
		AftaleGenoptagLog[] fraLog = this.getAftaleGenoptagLogFraAftale();
		if (fraLog != null && fraLog.length > 0) {
			if (this.equals(fraLog[0].getFraAftale())) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Undersøger om aftalen er en "ny" genoptaget aftale fra an anden ophørt aftale.
	 * 
	 * @return boolean
	 */
	public boolean isGenoptagetTilAftale() {
		AftaleGenoptagLog[] tilLog = this.getAftaleGenoptagLogTilAftale();
		if (tilLog != null && tilLog.length > 0) {
			if (this.equals(tilLog[0].getTilAftale())) {
				return true;
			}
		}
		return false;
	}

	// /**
	// *
	// * @return
	// */
	// public boolean isNyIkraftFraAftale(){
	//		
	// return false;
	// }
	//	
	// /**
	// *
	// * @return
	// */
	// public boolean isNyIkraftTilAftale(){
	//		
	// return false;
	// }

	public AftaleFlytningLog[] getAftaleFlytningLogFraAftale() {
		return (AftaleFlytningLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleFlytningLogFraAftale");
	}

	public AftaleFlytningLog[] getAftaleFlytningLogTilAftale() {
		return (AftaleFlytningLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleFlytningLogTilAftale");
	}

	public void addAftaleFlytningLogTilAftale(AftaleFlytningLog pAftaleFlytningLogTilAftale) {
		PersistensService.addToCollection(this, "AftaleFlytningLogTilAftale", pAftaleFlytningLogTilAftale);
	}

	public void addAftaleFlytningLogFraAftale(AftaleFlytningLog pAftaleFlytningLogFraAftale) {
		PersistensService.addToCollection(this, "AftaleFlytningLogFraAftale", pAftaleFlytningLogFraAftale);
	}

	// Logning af NyIkraft funktionen
	public AftaleNyIkraftLog[] getAftaleNyIkraftLogFraAftale() {
		return (AftaleNyIkraftLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleNyIkraftLogFraAftale");
	}

	public AftaleNyIkraftLog[] getAftaleNyIkraftLogTilAftale() {
		return (AftaleNyIkraftLog[]) DBServer.getInstance().getVbsf().get(this, "AftaleNyIkraftLogTilAftale");
	}

	public void addAftaleNyIkraftLogTilAftale(AftaleNyIkraftLog pAftaleNyIkraftLogTilAftale) {
		PersistensService.addToCollection(this, "AftaleNyIkraftLogTilAftale", pAftaleNyIkraftLogTilAftale);
	}

	public void addAftaleNyIkraftLogFraAftale(AftaleNyIkraftLog pAftaleNyIkraftLogFraAftale) {
		PersistensService.addToCollection(this, "AftaleNyIkraftLogFraAftale", pAftaleNyIkraftLogFraAftale);
	}

	/**
	 * Afgør om aftalen er en fra-aftale i forbindelse med en ny ikraft.
	 */
	public boolean isNyIkraftFraAftale() {
		AftaleNyIkraftLog[] anil = getAftaleNyIkraftLogFraAftale();
		return anil != null && anil.length > 0;
	}

	/**
	 * Afgør om aftalen er en til-aftale i forbindelse med en ny ikraft.
	 */
	public boolean isNyIkraftTilAftale() {
		AftaleNyIkraftLog[] anil = getAftaleNyIkraftLogTilAftale();
		return anil != null && anil.length > 0;
	}

	/**
	 * Afgører om aftalen er en fra-aftale.
	 */
	public boolean isFraAftale() {
		AftaleFlytningLog[] fraLog = this.getAftaleFlytningLogFraAftale();
		if (fraLog != null && fraLog.length > 0)
			return true;
		return false;
	}

	/**
	 * Afgører om aftalen er en til-aftale.
	 */
	public boolean isTilAftale() {
		AftaleFlytningLog[] tilLog = this.getAftaleFlytningLogTilAftale();
		if (tilLog != null && tilLog.length > 0)
			return true;
		return false;
	}

	public Aftale getAftaleDenAfloeste() {
		AftaleFlytningLog[] aftaleFlytningLogFraAftale = getAftaleFlytningLogTilAftale();
		if (aftaleFlytningLogFraAftale != null) {
			for (AftaleFlytningLog aftaleFlytningLog : aftaleFlytningLogFraAftale) {
				return aftaleFlytningLog.getFraAftale();
			}
		}
		AftaleGenoptagLog[] aftaleGenoptagLogFraAftale = getAftaleGenoptagLogTilAftale();
		if (aftaleGenoptagLogFraAftale != null) {
			for (AftaleGenoptagLog aftaleGenoptagLog : aftaleGenoptagLogFraAftale) {
				 return aftaleGenoptagLog.getFraAftale();
			}
		}
		AftaleNyIkraftLog[] aftaleNyIkraftLogFraAftale = getAftaleNyIkraftLogTilAftale();
		if (aftaleNyIkraftLogFraAftale != null) {
			for (AftaleNyIkraftLog aftaleNyikraftLog : aftaleNyIkraftLogFraAftale) {
				return aftaleNyikraftLog.getFraAftale();
			}
		}
		return null;
	}

	public Set<Aftale> collectAftaleForhold() {
		Set<Aftale> collection = new HashSet<Aftale>();

		if(this.isKopiTilbudAktivtEllerInAktiv()) {
			GsproMapningService gsproMapningService = new GsproMapningService();
			gsproMapningService.setModelObjekt_(this);
			GsproMapning[] gsproMapninger = gsproMapningService.getGsproMapningAktiveOgInaktive();
			if(gsproMapninger != null) {
				for (GsproMapning gsproMapning : gsproMapninger) {
					collection.add(gsproMapning.getAftaleFra());
//					collection.add(gsproMapning.getAftaleTil());
				}
			}
		}

		AftaleFlytningLog[] aftaleFlytningLogFraAftale = getAftaleFlytningLogFraAftale();
		if (aftaleFlytningLogFraAftale != null) {
			for (AftaleFlytningLog aftaleFlytningLog : aftaleFlytningLogFraAftale) {
	            collection.add(aftaleFlytningLog.getTilAftale());
	            collection.add(aftaleFlytningLog.getFraAftale());
            }
		}
		aftaleFlytningLogFraAftale = getAftaleFlytningLogTilAftale();
		if (aftaleFlytningLogFraAftale != null) {
			for (AftaleFlytningLog aftaleFlytningLog : aftaleFlytningLogFraAftale) {
	            collection.add(aftaleFlytningLog.getTilAftale());
	            collection.add(aftaleFlytningLog.getFraAftale());
            }
		}
		AftaleGenoptagLog[] aftaleGenoptagLogFraAftale = getAftaleGenoptagLogFraAftale();
		if (aftaleGenoptagLogFraAftale != null) {
			for (AftaleGenoptagLog aftaleGenoptagLog : aftaleGenoptagLogFraAftale) {
	            collection.add(aftaleGenoptagLog.getTilAftale());
	            collection.add(aftaleGenoptagLog.getFraAftale());
            }
		}
		aftaleGenoptagLogFraAftale = getAftaleGenoptagLogTilAftale();
		if (aftaleGenoptagLogFraAftale != null) {
			for (AftaleGenoptagLog aftaleGenoptagLog : aftaleGenoptagLogFraAftale) {
	            collection.add(aftaleGenoptagLog.getTilAftale());
	            collection.add(aftaleGenoptagLog.getFraAftale());
            }
		}
		AftaleNyIkraftLog[] aftaleNyIkraftLogFraAftale = getAftaleNyIkraftLogFraAftale();
		if (aftaleNyIkraftLogFraAftale != null) {
			for (AftaleNyIkraftLog aftaleNyikraftLog : aftaleNyIkraftLogFraAftale) {
	            collection.add(aftaleNyikraftLog.getTilAftale());
	            collection.add(aftaleNyikraftLog.getFraAftale());
            }
		}
		aftaleNyIkraftLogFraAftale = getAftaleNyIkraftLogTilAftale();
		if (aftaleNyIkraftLogFraAftale != null) {
			for (AftaleNyIkraftLog aftaleNyikraftLog : aftaleNyIkraftLogFraAftale) {
	            collection.add(aftaleNyikraftLog.getTilAftale());
	            collection.add(aftaleNyikraftLog.getFraAftale());
            }
		}

		collection.remove(this);
		return collection;
	}
	
	public Set<Aftale> collectAlleAftaleForhold() {
		Set<Aftale> collection = new HashSet<Aftale>();
		addAftaleForholdToCollection(this,collection);
		collection.remove(this);
		return collection;
	}

	/**
	 * Opsamler rekursivt alle de forsikringer som en given forsikring har et forhold til.
	 * 
	 * @param pForsikring
	 *            Den forsikring der skal opsamles for
	 * @param pCollection
	 *            Opsamlig af forsikringer
	 */
	private void addAftaleForholdToCollection(Aftale pForsikring, Set<Aftale> pCollection) {
		pCollection.add(pForsikring);
		for (Aftale aftale : pForsikring.collectAftaleForhold()) {
			if (!pCollection.contains(aftale)) {
				addAftaleForholdToCollection(aftale, pCollection);
			}
		}
	}

	/**
	 *
	 * @return regelrelationen til datarelationen AftaleTotalkundetype, null hvis ingen datarelation
	 */
	private TotalkundetypeAftaletype getTotalkundetypeAftaletype (BigDecimal pDato) {
		
		AftaleTotalkundetype aftaleTotalkundetype = getAftaleTotalkundetype(pDato);

		if (aftaleTotalkundetype != null) {
			// hvis der findes en aftaleTotalkundetype vil kan det være en diplomaftale, hvis
			// typen af forsikring samtidig er en diplomtype
			Aftaletype aftaletype = getAftaleTypen();

			// udfra aftaletype hentes totalkundetypeAftaletype, og herfra kan
			// man se om det er en diplomtype
			TotalkundetypeAftaletype[] totalkundetypeAftaletype = aftaletype.getTotalkundetypeAftaletype();

			if (totalkundetypeAftaletype != null) {
				return totalkundetypeAftaletype[0];
			}
		}
		return null;
	}
	/**
	 * Afgør om aftalen er en diplomaftale.
	 * 
	 * @return true hvis aftalen er en diplomaftale pr datoen
	 */
	public boolean isDiplomAftale(BigDecimal pDato) {
		
		TotalkundetypeAftaletype totalkundetypeAftaletype = this.getTotalkundetypeAftaletype(pDato);
		
		if (totalkundetypeAftaletype != null) {
			return totalkundetypeAftaletype.isDiplom();
		}
		
		return false;
	}
//	/**
//	 * Afgør om aftalen har været en diplomaftale.
//	 *
//	 * @return true hvis aftalen har været en diplomaftale
//	 */
//	public boolean harVaeretDiplomAftale(BigDecimal pDato) {
//
//		TotalkundetypeAftaletype totalkundetypeAftaletype = this.getTotalkundetypeAftaletype(pDato);
//
//		if (totalkundetypeAftaletype != null) {
//			return totalkundetypeAftaletype.isDiplom();
//		}
//
//		return false;
//	}
	/**
	 * Afgør om aftalen har en diplom indgang (AftaleTotalKundetype) der er annulleret.
	 * Skal kaldes inden opdatering, da annullerede indgange slettes.
	 * @return true hvis diplom indgang er annulleret.
	 */
	public boolean harAftaleDiplomMedstartPaaDatoenEllerSenere(BigDecimal pDato) {
		
		AftaleImpl aftale = this;
		if(aftale.isDiplomAftale(pDato)) {
			
			AftaleTotalkundetype aftaleTotalkundetype = getAftaleTotalkundetype(pDato);
			if (aftaleTotalkundetype != null) {
				TotalkundetypeAftaletype tkaftp = this.getTotalkundetypeAftaletype(pDato);
				if(tkaftp.isDiplom()) {
					if(aftaleTotalkundetype.getGld().compareTo(pDato) >= 0) {
						return true;
					}
				}
			}
		}
		return false;
		
	}
	
	/**
	 * Afgøre om en aftale bliver diplom i fremtid i forhold til 
	 * medsendte dato.
	 * 
	 * @return false, hvis forsikringer diplom pr. pDato eller 
	 * forsikringen ikke bliver diplom i fremtid.
	 */
	public boolean isDiplomFremtid(BigDecimal pDato){
		AftaleImpl aftale = this;
		if(!aftale.getAftaleTypen().isDiplomType()){
			return false;
		}
		if(aftale.isDiplomAftale(pDato)) {
			return false;
		} else {
			AftaleTotalkundetype[] aftaleTotalkundetype = getAftaleTotalkundetype();
			if (aftaleTotalkundetype != null) {
				for (AftaleTotalkundetype aftktyper : aftaleTotalkundetype) {
					BigDecimal fremDato = aftktyper.getGld();
					if(Datobehandling.isFremtidig(fremDato, pDato)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * tjek om forsikringen er DIP markeret på en given dato
	 * 
	 * @return true hvis aftalen er DIP markeret på den søgte dato
	 */
	public boolean isDIPMarkeret(BigDecimal pDato) {
		// opret en aftaleEgenskabsGruppe af typen DIP, som kan bruges til at søge med
		Aftaleegngrp dipEgenskabsGruppe = (Aftaleegngrp) AftaleegngrpImpl.getEgenskabKortnavn(Aftaleegngrp.DIPLOMBIL_MANUEL);
		AftaleAfegn dipEgenskab = getAftaleAfegn(dipEgenskabsGruppe, pDato);
		if (dipEgenskab != null) {
			return dipEgenskab.getEgenskab().getBenaevnelseTrim().equals("Ja");
		}
		return false;
	}

	/**
	 * Afgører om aftalen har været involveret i en flytning, endten som fra eller til aftale
	 * 
	 * @return true hvis aftalen har været involveret i en flytning.
	 */
	public boolean isFraEllerTilAftale() {
		if (isFraAftale() || isTilAftale())
			return true;
		return false;
	}

	/**
	 * Afgører om aftalen kan reguleres.
	 */
	public boolean isReguleringsForsikring(BigDecimal pDato) {
		boolean retur = false;
		AftpRgtp[] afTpRgTp = this.getAftaleTypen().getAftaletypeReguleringstype(pDato);
		for (int i = 0; afTpRgTp != null && i < afTpRgTp.length; i++) {
			AftpRgtp aRegulering = afTpRgTp[i];
			if (aRegulering.getReguleringstype().isForsikringsRegulering()) {
				retur = true;
				break;
			}
		}
		return retur;
	}

	// genererede accessmetoder til FordringRabatAft
	public FordringRabatAft[] getFordringRabatAft() {
		return (FordringRabatAft[]) DBServer.getInstance().getVbsf().get(this, "FordringRabatAft");
	}

	public void addFordringRabatAft(FordringRabatAft pFordringRabatAft) {
		PersistensService.addToCollection(this, "FordringRabatAft", pFordringRabatAft);
	}

	public void removeFordringRabatAft(FordringRabatAft oldFordringRabatAft) {
		PersistensService.removeFromCollection(this, "FordringRabatAft", oldFordringRabatAft);
	}

	// genererede accessmetoder til FordringOmkostningstypeAftale
	public FordringOmkostningstypeAftale[] getFordringOmkostningstypeAftale() {
		return (FordringOmkostningstypeAftale[]) DBServer.getInstance().getVbsf().get(this, "FordringOmkostningstypeAftale");
	}

	public void addFordringOmkostningstypeAftale(FordringOmkostningstypeAftale pFordringOmkostningstypeAftale) {
		PersistensService.addToCollection(this, "FordringOmkostningstypeAftale", pFordringOmkostningstypeAftale);
	}

	public void removeFordringOmkostningstypeAftale(FordringOmkostningstypeAftale oldFordringOmkostningstypeAftale) {
		PersistensService.removeFromCollection(this, "FordringOmkostningstypeAftale", oldFordringOmkostningstypeAftale);
	}

	// genererede accessmetoder til AftaleOmkFritagelseFordring
	public AftaleOmkFritagelseFordring[] getAftaleOmkFritagelseFordring() {
		return (AftaleOmkFritagelseFordring[]) DBServer.getInstance().getVbsf().get(this, "AftaleOmkFritagelseFordring");
	}

	public void addAftaleOmkFritagelseFordring(AftaleOmkFritagelseFordring pAftaleOmkFritagelseFordring) {
		PersistensService.addToCollection(this, "AftaleOmkFritagelseFordring", pAftaleOmkFritagelseFordring);
	}

	public void removeAftaleOmkFritagelseFordring(AftaleOmkFritagelseFordring oldAftaleOmkFritagelseFordring) {
		PersistensService.removeFromCollection(this, "AftaleOmkFritagelseFordring", oldAftaleOmkFritagelseFordring);
	}

	// genererede accessmetoder til EgenbetaltOmkostFordring
	public EgenbetaltOmkostFordring[] getEgenbetaltOmkostFordring() {
		return (EgenbetaltOmkostFordring[]) DBServer.getInstance().getVbsf().get(this, "EgenbetaltOmkostFordring");
	}

	public void addEgenbetaltOmkostFordring(EgenbetaltOmkostFordring pEgenbetaltOmkostFordring) {
		PersistensService.addToCollection(this, "EgenbetaltOmkostFordring", pEgenbetaltOmkostFordring);
	}

	public void removeEgenbetaltOmkostFordring(EgenbetaltOmkostFordring oldEgenbetaltOmkostFordring) {
		PersistensService.removeFromCollection(this, "EgenbetaltOmkostFordring", oldEgenbetaltOmkostFordring);
	}

	// genererede accessmetoder til EgenbetaltStormflod
	public EgenbetaltStormflod[] getEgenbetaltStormflod() {
		return (EgenbetaltStormflod[]) DBServer.getInstance().getVbsf().get(this, "EgenbetaltStormflod");
	}

	public void addEgenbetaltStormflod(EgenbetaltStormflod pEgenbetaltStormflod) {
		PersistensService.addToCollection(this, "EgenbetaltStormflod", pEgenbetaltStormflod);
	}

	public void removeEgenbetaltStormflod(EgenbetaltStormflod oldEgenbetaltStormflod) {
		PersistensService.removeFromCollection(this, "EgenbetaltStormflod", oldEgenbetaltStormflod);
	}

	// genererede accessmetoder til ReaKontrakt
	public ReaKontrakt[] getReaKontrakt() {
		return (ReaKontrakt[]) DBServer.getInstance().getVbsf().get(this, "ReaKontrakt");
	}

	public void addReaKontrakt(ReaKontrakt pReaKontrakt) {
		PersistensService.addToCollection(this, "ReaKontrakt", pReaKontrakt);
	}

	public void removeReaKontrakt(ReaKontrakt oldReaKontrakt) {
		PersistensService.removeFromCollection(this, "ReaKontrakt", oldReaKontrakt);
	}

	/**
	 * Markering de collections der holder provision og foreløbig provision som dirty.
	 */
	public void refreshProvision() {
		DBServer.getInstance().getVbsf().markCollectionDirty(this, "Provision");
		DBServer.getInstance().getVbsf().markCollectionDirty(this, "ForeloebigProvision");
	}

	// genererede accessmetoder til Provision
	public Provision[] getProvision() {
		return (Provision[]) DBServer.getInstance().getVbsf().get(this, "Provision");
	}

	public void addProvision(Provision pProvision) {
		PersistensService.addToCollection(this, "Provision", pProvision);
	}

	public void removeProvision(Provision oldProvision) {
		PersistensService.removeFromCollection(this, "Provision", oldProvision);
	}

	/**
	 * @return Liste med <code>BigDecimals</code> hvor elementerne ligger pakket i rækkefølgen:
	 *         <ol>
	 *         <li>provision summeret på foreløbig, provision
	 *         <li>der afventer indbetaling,
	 *         <li>provision der afventer udbetaling og
	 *         <li>udbetalt provision.
	 *         </ol>
	 */
	public List<BigDecimal> getProvisionsBeloebSummeret() {
		List<BigDecimal> beloeb = new ArrayList<BigDecimal>(4);
		BigDecimal foreloebig = GaiaConst.NULBD;
		BigDecimal afvIndbetaling = GaiaConst.NULBD;
		BigDecimal afvUdbetaling = GaiaConst.NULBD;
		BigDecimal udbetalt = GaiaConst.NULBD;

		// foreloebig provision
		Provision[] prov = this.getForeloebigProvision();
		if (prov != null && prov.length > 0) {
			for (int i = 0; i < prov.length; i++) {
				foreloebig = foreloebig.add(prov[i].getProvisionsbeloeb());
			}
		}
		beloeb.add(foreloebig);

		prov = this.getProvision();
		if (prov != null && prov.length > 0) {
			for (int i = 0; i < prov.length; i++) {
				Provision p = prov[i];
				if (p.getProvisionExtended() != null &&
						p.getProvisionExtended().isServiceringsHonorar())
					continue; // #26144 Skal ikke med i oversigten

				switch (p.getProvisionsBeloebType()) {
				case dk.gensam.gaia.model.provision.Provision.AFV_INDBETALING: {
					afvIndbetaling = afvIndbetaling.add(p.getProvisionsbeloeb());
					break;
				}
				case dk.gensam.gaia.model.provision.Provision.AFV_UDBETALING: {
					afvUdbetaling = afvUdbetaling.add(p.getProvisionsbeloeb());
					break;
				}
				case dk.gensam.gaia.model.provision.Provision.UDBETALT: {
					udbetalt = udbetalt.add(p.getProvisionsbeloeb());
					break;
				}
				}
			}
		}
		beloeb.add(afvIndbetaling);
		beloeb.add(afvUdbetaling);
		beloeb.add(udbetalt);

		return beloeb;
	}

	/**
	 * Returnerer samme resultat som public List getProvisionsBeloebSummeret() bortset fra
	 * at denne kun tager dem som er aktuelle for individet der gives med.
	 * Hvis individet er null returneres resultatet af getProvisionsBeloebSummeret().
	 */
	public List<BigDecimal> getProvisionsBeloebSummeretPrProvisionsModtager(Individ pProvModtager) {
		if (pProvModtager == null) return getProvisionsBeloebSummeret();
		List<BigDecimal> beloeb = new ArrayList<BigDecimal>(4);
		BigDecimal foreloebig = GaiaConst.NULBD;
		BigDecimal afvIndbetaling = GaiaConst.NULBD;
		BigDecimal afvUdbetaling = GaiaConst.NULBD;
		BigDecimal udbetalt = GaiaConst.NULBD;

		// foreloebig provision
		Provision[] prov = this.getForeloebigProvision();
		if (prov != null && prov.length > 0) {
			for (int i = 0; i < prov.length; i++) {
				// Hvis individet ikke er det samme fortsættes
				if (prov[i].getIndivid().getId().equals(pProvModtager.getId())) { 
					foreloebig = foreloebig.add(prov[i].getProvisionsbeloeb());
				}
			}
		}
		beloeb.add(foreloebig);

		prov = this.getProvision();
		if (prov != null && prov.length > 0) {
			for (int i = 0; i < prov.length; i++) {
				Provision p = prov[i];
				// Hvis individet ikke er det samme fortsættes
				if (!p.getIndivid().getId().equals(pProvModtager.getId())) { 
					continue;
				}
				
				switch (p.getProvisionsBeloebType()) {
				case dk.gensam.gaia.model.provision.Provision.AFV_INDBETALING: {
					afvIndbetaling = afvIndbetaling.add(p.getProvisionsbeloeb());
					break;
				}
				case dk.gensam.gaia.model.provision.Provision.AFV_UDBETALING: {
					afvUdbetaling = afvUdbetaling.add(p.getProvisionsbeloeb());
					break;
				}
				case dk.gensam.gaia.model.provision.Provision.UDBETALT: {
					udbetalt = udbetalt.add(p.getProvisionsbeloeb());
					break;
				}
				}
			}
		}
		beloeb.add(afvIndbetaling);
		beloeb.add(afvUdbetaling);
		beloeb.add(udbetalt);

		return beloeb;
	}

	/**
	 * Returnerer Policenummeret gældende pr. den angivne dato -- NB ! Udenom Egenskabssystem.
	 */
	public Egenskab getPolicenummerDirekte(BigDecimal pDato) {
		AftaleAfegn afafeg = getAftaleAfegn(AftaleegngrpImpl.getAftaleegngrpPolicenummer(), this.getOpgDatoKorrigeret(pDato));
		if (afafeg != null) {
			return afafeg.getEgenskab();
		}
		else {
			if (this.isAnonymiseret()) {
				return AftaleegngrpImpl.getAftaleegngrpPolicenummer().getDummyEgenskabForAnonym();
			}
		}

		return null;
	}

	/**
	 * Returnerer GammelPolicenummer fra konverteringen gældende pr. den angivne dato -- NB ! Udenom Egenskabssystem.
	 */
	public Egenskab getPolicenummerDirekteGammelt(BigDecimal pDato) {
		AftaleAfegn afafeg = getAftaleAfegn(AftaleegngrpImpl.getAftaleegngrpPolicenummerGammelt(), this.getOpgDatoKorrigeret(pDato));
		if (afafeg != null) {
			return afafeg.getEgenskab();
		}
		else {
			if (this.isAnonymiseret()) {
				return AftaleegngrpImpl.getAftaleegngrpPolicenummerGammelt().getDummyEgenskabForAnonym();
			}
		}

		return null;
	}

	/**
	 * Returnerer Aftalens kontodimension
	 */
	public String getKontodimension(BigDecimal pDato) {
		if ((kontodimension == null) || (Periode.compare(pDato, true, kontodimensionEgenskab.getGld(), true) == Periode.SMALLER)
				|| (Periode.compare(pDato, false, kontodimensionEgenskab.getOph(), false) == Periode.BIGGER)) {
			Egenskab afeg = getPolicenummerDirekte(pDato);
			if (afeg != null) {
				kontodimensionEgenskab = afeg;
				kontodimension = kontodimensionEgenskab.getBenaevnelse().trim();
			}
		}

		return kontodimension;
	}
	/**
	 *
	 * get alle afregninger tilhørende aftalen EXCL. præmie-ristorno
	 * først kaldes getAfregningerAlle, dernæst fjernes præmie-ristorno afregninger.

	 * @return {@link ArrayList} af afregninger
	 *
	 */
	public ArrayList<Afregning> getAfregningerAlleExclPraemieRistorno() {

		ArrayList<Afregning> afregninger = this.getAfregningerAlle();
		ArrayList<Afregning> afregningerExclPraemieRistorno = new ArrayList<>();
		if(afregninger != null) {
			for (Afregning afregning : afregninger) {
				if(!afregning.getAfregningsform().isPraemieRistorno()) {
					afregningerExclPraemieRistorno.add(afregning);
				}
			}
		}
		if(afregningerExclPraemieRistorno.size() > 0) {
			return afregningerExclPraemieRistorno;
		}
		return null;
	}
	/**
	 * 
	 * get alle afregninger tilhørende aftalen
	 * først hentes afregninger der hænger direkte på aftalen,
	 * dernæst fremfindes alle topafregninger på de før fremfundne afregninger
	 * dernæste findes alle underliggende afregninger til alle topafregninger frem
	 * der add til {@link ArrayList} der returneres både topafregninger og underliggende afregninger 
	 * @return {@link ArrayList} af afregninger
	 *
	 */
	public ArrayList<Afregning> getAfregningerAlle() {
		
		// find direkte afregninger på aftalen
		Afregning[] arDirekte = this.getAfregning();
		if (arDirekte==null) return null;
		
		// find alle topafregninger
		ArrayList<Afregning> arTops = new ArrayList<Afregning>();
		for (Afregning arDirect : arDirekte) {
			if(!arTops.contains(arDirect.getAfregningTop())) {
				arTops.add(arDirect.getAfregningTop());
			}
			
		}

		// add top og underliggende afregninger til retur ArrayList
		ArrayList<Afregning> arAlle = new ArrayList<Afregning>();
		for (Afregning arTop : arTops) {
			if(!arAlle.contains(arTop)) {
				arAlle.add(arTop);
			}
			Afregning[] arUnders = arTop.getAfregningerUnder();
			if(arUnders!=null) {
				for (Afregning arUnder : arUnders) {
					if(!arAlle.contains(arUnder)) {
						arAlle.add(arUnder);
					}
				}
			}
			
		}
		return arAlle;
	}

	public List<Afregning> getAfregningerGrundOgSaldooverfoersler() {
		List<Afregning> retur = new ArrayList<>();
		Afregning[] afregninger = getAfregning(); // står nu med liste af grundafregninger
		retur = ContainerUtil.asList(afregninger);
		
		List<Afregning> saldoAfregninger = getTegnesAfIndivid().getSaldoAfregninger();
		if (afregninger != null && saldoAfregninger != null) {
			for (Afregning saldoafregning : saldoAfregninger) {
				Set<Afregning> afregningerTo = AfregningImpl.getAfregninger(saldoafregning.getUdsendelsesId());
				for (Afregning afregning : afregningerTo) {
					if (afregning.getUdsendelsesId().equals(saldoafregning.getUdsendelsesId())) {
						if (afregning.contains(this, false) && !retur.contains(saldoafregning))
							retur.add(saldoafregning);
					}
				}
			}
		}
		return retur;
	}

	// genererede accessmetoder til Afregningextended
	public Afregning[] getAfregning() {
		return (Afregning[]) DBServer.getInstance().getVbsf().get(this, "Afregning");
	}

	public void addAfregning(Afregning pAfregning) {
		PersistensService.addToCollection(this, "Afregning", pAfregning);
	}

	public void removeAfregning(Afregning oldAfregning) {
		PersistensService.removeFromCollection(this, "Afregning", oldAfregning);
	}

	// genererede accessmetoder til AftaleAarsAfgiftFordring
	public AftaleAarsAfgiftFordring[] getAftaleAarsAfgiftFordring() {
		return (AftaleAarsAfgiftFordring[]) DBServer.getInstance().getVbsf().get(this, "AftaleAarsAfgiftFordring");
	}

	public AftaleAarsAfgiftFordring[] getAftaleAarsAfgiftFordring(BigDecimal pDato) {
		// Debug.setDebugging(Debugger.DATABASE+Debugger.DEEP+Debugger.PERSISTENCE);
		return (AftaleAarsAfgiftFordring[]) DBServer.getInstance().getVbsf().get(this, "AftaleAarsAfgiftFordring", pDato);
	}

	public void addAftaleAarsAfgiftFordring(AftaleAarsAfgiftFordring pAftaleAarsAfgiftFordring) {
		PersistensService.addToCollection(this, "AftaleAarsAfgiftFordring", pAftaleAarsAfgiftFordring);
	}

	public void removeAftaleAarsAfgiftFordring(AftaleAarsAfgiftFordring oldAftaleAarsAfgiftFordring) {
		PersistensService.removeFromCollection(this, "AftaleAarsAfgiftFordring", oldAftaleAarsAfgiftFordring);
	}

	// genererede accessmetoder til EgenbetaltAftaleAarsAfgiftFord
	public EgenbetaltAftaleAarsAfgiftFord[] getEgenbetaltAftaleAarsAfgiftFord() {
		return (EgenbetaltAftaleAarsAfgiftFord[]) DBServer.getInstance().getVbsf().get(this, "EgenbetaltAftaleAarsAfgiftFord");
	}

	public void addEgenbetaltAftaleAarsAfgiftFord(EgenbetaltAftaleAarsAfgiftFord pEgenbetaltAftaleAarsAfgiftFord) {
		PersistensService.addToCollection(this, "EgenbetaltAftaleAarsAfgiftFord", pEgenbetaltAftaleAarsAfgiftFord);
	}

	public void removeEgenbetaltAftaleAarsAfgiftFord(EgenbetaltAftaleAarsAfgiftFord oldEgenbetaltAftaleAarsAfgiftFord) {
		PersistensService.removeFromCollection(this, "EgenbetaltAftaleAarsAfgiftFord", oldEgenbetaltAftaleAarsAfgiftFord);
	}

	// genererede accessmetoder til FiktivAftaleAarsAfgift
	public FiktivAftaleAarsAfgift[] getFiktivAftaleAarsAfgift() {
		return (FiktivAftaleAarsAfgift[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleAarsAfgift");
	}

	public FiktivAftaleAarsAfgift[] getFiktivAftaleAarsAfgift(BigDecimal pDato) {
		return (FiktivAftaleAarsAfgift[]) DBServer.getInstance().getVbsf().get(this, "FiktivAftaleAarsAfgift", pDato);
	}

	public void addFiktivAftaleAarsAfgift(FiktivAftaleAarsAfgift pFiktivAftaleAarsAfgift) {
		PersistensService.addToCollection(this, "FiktivAftaleAarsAfgift", pFiktivAftaleAarsAfgift);
	}

	public void removeFiktivAftaleAarsAfgift(FiktivAftaleAarsAfgift oldFiktivAftaleAarsAfgift) {
		PersistensService.removeFromCollection(this, "FiktivAftaleAarsAfgift", oldFiktivAftaleAarsAfgift);
	}

	public Aftale findAftaleHenvist() {
		String qryIn = "SELECT AFID FROM henvisaf";
		OQuery qry = DBServer.getVbsfInst().queryCreate(AftaleImpl.class);
		qry.add(this.getOprBruger(), "oprBruger", OQuery.EQUAL);
		qry.add(this.getOprDato(), "oprDato", OQuery.EQUAL);
		qry.add(this.getOprTid(), "oprTid", OQuery.EQUAL);
		qry.add(qryIn, "aftaleId", OQuery.IN);
		Aftale[] aftaleTab = (Aftale[]) DBServer.getVbsfInst().queryExecute(qry);
		if (aftaleTab == null) {
			return null;
		}
		for (Aftale aftale : aftaleTab) {
			if (this.getPolicenummerDirekte(this.getGld()).equals(aftale.getPolicenummerDirekte(aftale.getGld()))) {
				return aftale;
			}
		}
		return null;
	}
	public Aftalehaendelse getAftalehaendelseFoersteAendring() {
		Aftalehaendelse[] aftalehaendelseTab = this.getAftalehaendelse();
		Arrays.sort(aftalehaendelseTab, new ModelObjektIdComparator());
		for (Aftalehaendelse aftalehaendelse : aftalehaendelseTab){
			if (aftalehaendelse.getHaendelsestype().equals(Aftalehaendelse.HAENDELSESTYPE_AENDRING)){
				// Indtil det modsatte bliver bevist skal brugeren også findes i *FILE BRGPRVM
				// ellers har vi ikke fundet den "første" ændringshændelse endnu.
				if (BrugerProvisionsmodtagerImpl.getAlleProvisionsmodtagereForBruger(aftalehaendelse.getOprBruger().trim()).size() > 0) {
    				return aftalehaendelse;
				}
			}
		}
		return null;
	}

	public Aftalehaendelse getAftalehaendelseNyeste(BigDecimal pDato, boolean prvFilter) {
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);
		qry.add(aftaleId, "Aftale", OQuery.EQUAL, OQuery.AND);
		qry.add(pDato, "haendelsesdato", OQuery.EQUAL, OQuery.AND);
		if (prvFilter) {
			qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
		}
		qry.addOrder("oprDato", OQuery.DESC);
		qry.addOrder("oprTid", OQuery.DESC);
		qry.setMaxCount(1);

		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null && tmpah.length > 0) {
			// Så har vi een af de nyeste, find så den nyeste hvis der er flere der er oprettet på samme tidspunkt.
			Aftalehaendelse ah = tmpah[0];
			qry = QueryService.queryCreate(AftalehaendelseImpl.class);
			qry.add(ah.getAftale().getAftaleId(), "Aftale", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getHaendelsesdato(), "haendelsesdato", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getOprDato(), "oprDato", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getOprTid(), "oprTid", OQuery.EQUAL, OQuery.AND);
			if (prvFilter) {
				qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
			}

			tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
			return findNyesteAftaleHaendelse(tmpah);
		}
		return null;
	}

	public Aftalehaendelse getAftalehaendelseNaestNyesteEllerNyeste(BigDecimal pDato, boolean prvFilter) {
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);
		qry.add(aftaleId, "Aftale", OQuery.EQUAL, OQuery.AND);
		qry.add(pDato, "haendelsesdato", OQuery.EQUAL, OQuery.AND);
		if (prvFilter) {
			qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
		}
		qry.addOrder("oprDato", OQuery.DESC);
		qry.addOrder("oprTid", OQuery.DESC);
		qry.setMaxCount(2);

		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null) {
			if (tmpah.length > 1) {
				// Så har vi een af de nyeste, find så den nyeste hvis der er flere der er oprettet på samme tidspunkt.
				Aftalehaendelse ah = tmpah[1];
				qry = QueryService.queryCreate(AftalehaendelseImpl.class);
				qry.add(ah.getAftale().getAftaleId(), "Aftale", OQuery.EQUAL, OQuery.AND);
				qry.add(ah.getHaendelsesdato(), "haendelsesdato", OQuery.EQUAL, OQuery.AND);
				qry.add(ah.getOprDato(), "oprDato", OQuery.EQUAL, OQuery.AND);
				qry.add(ah.getOprTid(), "oprTid", OQuery.EQUAL, OQuery.AND);
				if (prvFilter) {
					qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
				}

				tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
				return findNyesteAftaleHaendelse(tmpah);
			} else if (tmpah.length == 1) {
				return tmpah[0];
			}
		}
		return null;
	}

	public Aftalehaendelse getAftalehaendelseNyesteMedProvisionsmodtager(BigDecimal pDato) {
		String clsAftalehaendelse = AftalehaendelseImpl.class.getName();
		String clsForsikringstagerAftalehaendelseProvisionmodtager = ForsikringstagerAftalehaendelseProvisionmodtagerImpl.class.getName();
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);
		qry.add(aftaleId, "Aftale", OQuery.EQUAL, OQuery.AND);
		qry.add(pDato, "haendelsesdato", OQuery.EQUAL, OQuery.AND);
		qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
		qry.addJoin(clsAftalehaendelse + ".aftalehaendelseId", clsForsikringstagerAftalehaendelseProvisionmodtager + ".Aftalehaendelse");
		qry.add("", clsForsikringstagerAftalehaendelseProvisionmodtager + ".Provisionsmodtager", OQuery.NOT_EQUAL);
		qry.addOrder("oprDato", OQuery.DESC);
		qry.addOrder("oprTid", OQuery.DESC);
		qry.setMaxCount(1);

		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null && tmpah.length > 0) {
			// Så har vi een af de nyeste, find så den nyeste hvis der er flere der er oprettet på samme tidspunkt.
			Aftalehaendelse ah = tmpah[0];
			qry = QueryService.queryCreate(AftalehaendelseImpl.class);
			qry.add(ah.getAftale().getAftaleId(), "Aftale", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getHaendelsesdato(), "haendelsesdato", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getOprDato(), "oprDato", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getOprTid(), "oprTid", OQuery.EQUAL, OQuery.AND);
			qry.add(ProvisionsHaendelsesMapper.getIrrelevanteHaendelserAsSQLIN(), "haendelsestype", OQuery.NOT_IN, OQuery.AND);
			qry.addJoin(clsAftalehaendelse + ".aftalehaendelseId", clsForsikringstagerAftalehaendelseProvisionmodtager + ".Aftalehaendelse");
			qry.add("", clsForsikringstagerAftalehaendelseProvisionmodtager + ".Provisionsmodtager", OQuery.NOT_EQUAL);

			tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
			return findNyesteAftaleHaendelse(tmpah);
		}
		return null;
	}

	private Aftalehaendelse findNyesteAftaleHaendelse(Aftalehaendelse[] pAftalehaendelser) {
		if (pAftalehaendelser != null && pAftalehaendelser.length == 1) {
			return pAftalehaendelser[0];
		} else if (pAftalehaendelser != null && pAftalehaendelser.length > 1) {
			Aftalehaendelse nyesteHaendelse = null;
			BigInteger stoersteHaendelsesId = new BigInteger("0");
			for (int i = 0; i < pAftalehaendelser.length; i++) {
				if (stoersteHaendelsesId.compareTo(KonverterId.toDecimal(pAftalehaendelser[i].getId())) < 0) {
					stoersteHaendelsesId = KonverterId.toDecimal(pAftalehaendelser[i].getId());
					nyesteHaendelse = pAftalehaendelser[i];
				}
			}
			return nyesteHaendelse;
		}
		return null;
	}

	/**
	 * hvis der findes aftaleHændelser senere end medsendt dato med "AENDRING" i type
	 * returneres TRUE
	 * ellers
	 * returneres FALSE
	 * @param dato
	 * @return
	 */
	public boolean harFremtidigeAendringer(BigDecimal dato) {
		return getFremtidigeAendringer(dato) != null;
	}

	/**
	 * hvis der findes aftaleHændelser senere end medsendt dato med "AENDRING" i type
	 * returneres den nærmeste i fremtid
	 * ellers
	 * returneres null
	 * @param dato
	 * @return BigDecimal
	 */
	public BigDecimal getFremtidigeAendringer(BigDecimal dato) {
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);

		qry.add(aftaleId, "Aftale", OQuery.EQUAL);
		qry.add(dato, "haendelsesdato", OQuery.GREATER);
		qry.add("AENDRING" + "%", "haendelsestype", OQuery.LIKE);

		qry.addOrder("haendelsesdato", OQuery.ASC);
		qry.setMaxCount(1);
		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null && tmpah.length > 0) {
			return tmpah[0].getHaendelsesdato();
		}

		return null;
	}

	/**
	 * hvis der findes aftaleHændelser tidligere end medsendt dato med "AENDRING" i type
	 * returneres TRUE
	 * ellers
	 * returneres FALSE
	 * @param dato
	 * @return
	 */
	public boolean harFortidigeAendringer(BigDecimal dato) {
		return getFortidigeAendringer(dato) != null;
	}

	/**
	 * hvis der findes aftaleHændelser tidligere end medsendt dato med "AENDRING" i type
	 * returneres den nærmeste i fortid
	 * ellers
	 * returneres null
	 * @param dato
	 * @return BigDecimal
	 */
	public BigDecimal getFortidigeAendringer(BigDecimal dato) {
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);

		qry.add(aftaleId, "Aftale", OQuery.EQUAL);
		qry.add(dato, "haendelsesdato", OQuery.LESS);
		qry.add("AENDRING" + "%", "haendelsestype", OQuery.LIKE);

		qry.addOrder("haendelsesdato", OQuery.DESC);
		qry.setMaxCount(1);
		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null && tmpah.length > 0) {
			return tmpah[0].getHaendelsesdato();
		}

		return null;
	}

	public Aftalehaendelse getAftalehaendelseNyeste(BigDecimal dato, String type) {
		OQuery qry = QueryService.queryCreate(AftalehaendelseImpl.class);

		qry.add(aftaleId, "Aftale", OQuery.EQUAL);
		qry.add(dato, "haendelsesdato", OQuery.EQUAL);
		qry.add(type + "%", "haendelsestype", OQuery.LIKE);

		qry.addOrder("oprDato", OQuery.DESC);
		qry.addOrder("oprTid", OQuery.DESC);
		qry.setMaxCount(1);

		Aftalehaendelse[] tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (tmpah != null && tmpah.length > 0) {
			// Så har vi een af de nyeste, find så den nyeste hvis der er flere der er oprettet på samme tidspunkt.
			Aftalehaendelse ah = tmpah[0];
			qry = QueryService.queryCreate(AftalehaendelseImpl.class);

			qry.add(ah.getAftale().getAftaleId(), "Aftale", OQuery.EQUAL);
			qry.add(ah.getHaendelsesdato(), "haendelsesdato", OQuery.EQUAL);
			qry.add(type + "%", "haendelsestype", OQuery.LIKE);
			qry.add(ah.getOprDato(), "oprDato", OQuery.EQUAL, OQuery.AND);
			qry.add(ah.getOprTid(), "oprTid", OQuery.EQUAL, OQuery.AND);

			tmpah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().queryExecute(qry);
			return findNyesteAftaleHaendelse(tmpah);
		}

		return null;
	}

	// genererede accessmetoder til Aftalehaendelse
	public Aftalehaendelse[] getAftalehaendelse() {
		return (Aftalehaendelse[]) DBServer.getInstance().getVbsf().get(this, "Aftalehaendelse");
	}

	public Aftalehaendelse[] getAftalehaendelse(BigDecimal pDato) {
		Aftalehaendelse[] ah = (Aftalehaendelse[]) DBServer.getInstance().getVbsf().get(this, "Aftalehaendelse");
		if (ah == null)
			return null;
		// da aftalehændelse ikke har en ophørs-attribut, kan der ikke bruges normal date-query her.
		return (Aftalehaendelse[]) Datobehandling.findGaeldende(ah, pDato);
	}

	public void addAftalehaendelse(Aftalehaendelse pAftalehaendelse) {
		PersistensService.addToCollection(this, "Aftalehaendelse", pAftalehaendelse);
	}

	public void removeAftalehaendelse(Aftalehaendelse oldAftalehaendelse) {
		PersistensService.removeFromCollection(this, "Aftalehaendelse", oldAftalehaendelse);
	}

	public boolean isMarkeretEjSamle(){
		AftaleAfregningEjSamles[] afrEjSamles = getAftaleAfregningEjSamles();
		if (afrEjSamles != null && afrEjSamles.length > 0){
			return true;
		}
		return false;
	}

	// public boolean maaAftaleSamles(BigDecimal pDato){
	//  
	// return true;
	// }

	// genererede accessmetoder til AftaleAfregningEjSamles
	public AftaleAfregningEjSamles[] getAftaleAfregningEjSamles() {
		return (AftaleAfregningEjSamles[]) DBServer.getInstance().getVbsf().get(this, "AftaleAfregningEjSamles");
	}

	public AftaleAfregningEjSamles[] getAftaleAfregningEjSamles(BigDecimal pDato) {
		return (AftaleAfregningEjSamles[]) DBServer.getInstance().getVbsf().get(this, "AftaleAfregningEjSamles", pDato);
	}

	public void addAftaleAfregningEjSamles(AftaleAfregningEjSamles pAftaleAfregningEjSamles) {
		PersistensService.addToCollection(this, "AftaleAfregningEjSamles", pAftaleAfregningEjSamles);
	}

	public void removeAftaleAfregningEjSamles(AftaleAfregningEjSamles oldAftaleAfregningEjSamles) {
		PersistensService.removeFromCollection(this, "AftaleAfregningEjSamles", oldAftaleAfregningEjSamles);
	}

	public AftaleTotalkundetype[] getAftaleTotalkundetype() {
		return (AftaleTotalkundetype[]) DBServer.getInstance().getVbsf().get(this, AftaleTotalkundetype.AFTALETOTALKUNDETYPE);
	}
	public AftaleTotalkundetype[] getAftaleTotalkundetypeGldFremtidige(BigDecimal pDato) {
		return (AftaleTotalkundetype[]) DBServer.getInstance().getVbsf().get(this, pDato, AftaleTotalkundetype.AFTALETOTALKUNDETYPE);
	}

	public synchronized boolean harForsikringenTotalkundeRabat(BigDecimal pGld) {
		return getAftaleTotalkundetype(pGld) != null;
	}

	public AftaleTotalkundetype getAftaleTotalkundetype(BigDecimal pDato) {
		AftaleTotalkundetype[] afTktp = (AftaleTotalkundetype[]) DBServer.getInstance().getVbsf().get(this,
				AftaleTotalkundetype.AFTALETOTALKUNDETYPE, pDato);
		if (afTktp != null && afTktp.length > 0)
			return afTktp[0];
		return null;
	}

	public void addAftaleTotalkundetype(AftaleTotalkundetype pAftaleTotalkundetype) {
//		PersistensService.addToCollection(this, AftaleTotalkundetype.AFTALETOTALKUNDETYPE, pAftaleTotalkundetype);
	}

	public void removeAftaleTotalkundetype(AftaleTotalkundetype oldAftaleTotalkundetype) {
//		PersistensService.removeFromCollection(this, AftaleTotalkundetype.AFTALETOTALKUNDETYPE, oldAftaleTotalkundetype);
	}

	void debug(Object message) {
		log_.info("[AftaleImpl] " + message);
	}

	/**
	 * @return En dækning hvortil der må tilknyttes manuel provision pr. den givne dato. Null hvis ingen.
	 * 
	 * Hvis ikke aftalen har gældende dækninger pr. datoen eller dagen før (ophørstarifering) returneres null.
	 */
	public Produkt getDaekningManuelProvision(BigDecimal pDato) {
		Opgavetype[] opgtp = OpgavetypeImpl.getOpgavetyperOpkraevning();

		Produkt[] pd = this.getProduktGld(pDato); // vigtigt at hovedprodukter også returneres
		if (pd == null) {
			pd = this.getProduktGld(Datobehandling.datoPlusMinusAntal(pDato, -1));
		}
		int firstDaekningIx = -1;
		for (int p = 0; pd != null && p < pd.length; p++) {
			if (pd[p].isGrundprodukt()) {
				firstDaekningIx = p;
				break;
			}
		}
		if (firstDaekningIx < 0) {
			return null;
			// slet ingen dækninger pr. datoen - farvel
		}

		Dagbogsopgave[] opgaverAftale = this.getDagbogAftale(opgtp, null, pDato, 0);
		for (int a = 0; opgaverAftale != null && a < opgaverAftale.length; a++) {
			if (!opgaverAftale[a].isUdfoert())
				return pd[firstDaekningIx];
			// datoer pr. en kommende fornyelse / delopkrævning forlader os altså her - ok.
		}

		Dagbogsopgave[] opgaverProdukt;
		for (int p = 0; pd != null && p < pd.length; p++) {
			opgaverProdukt = pd[p].getDagbogProdukt(opgtp, null, pDato, 0);
			for (int a = 0; opgaverProdukt != null && a < opgaverProdukt.length; a++) {
				if (!opgaverProdukt[a].isUdfoert()) {
					if (pd[p].isGrundprodukt())
						return pd[p];
					else
						return pd[firstDaekningIx];
					// datoer pr. en kommende tarifering forlader os altså her - ok.
				}
			}
		}
		// så kom vi så langt. Der er altså ingen ikke-udførte opkrævningsopgaver pr. datoen
		// Ligger der så ikke-udlignede præmieopkrævninger pr. datoen ?

		OQuery qry1 = QueryService.queryCreate(FordringImpl.class);
		qry1.add(GaiaConst.NULBD, "udligningsdato", OQuery.EQUAL, OQuery.AND);
		qry1.add(GensamUtil.getIDsAsSQLIN(pd), "Produkt", OQuery.IN, OQuery.AND);
		qry1.add(pDato, "periodeFra", OQuery.EQUAL, OQuery.AND);
		qry1.add(GensamUtil.getIDsAsSQLIN(FordringstypeImpl.getFordringstypeProvision()), "Fordringstype", OQuery.NOT_IN, OQuery.AND); // provisionsfordringer
		qry1.setMaxCount(1);
		Fordring[] praeFordr = (Fordring[]) DBServer.getInstance().getVbsf().queryExecute(qry1);

		if (praeFordr == null)
			return null;
		// Alt er udlignet for denne forsikring - beklager

		// så fandt vi en dækning vi kunne bruge
		return praeFordr[0].getProdukt();
	}

	// genererede accessmetoder til ForeloebigProvision
	public Provision[] getForeloebigProvision() {
		return (Provision[]) DBServer.getInstance().getVbsf().get(this, "ForeloebigProvision");
	}

	public void addForeloebigProvision(Provision pForeloebigProvision) {
		PersistensService.addToCollection(this, "ForeloebigProvision", pForeloebigProvision);
	}

	public void removeForeloebigProvision(Provision oldForeloebigProvision) {
		PersistensService.removeFromCollection(this, "ForeloebigProvision", oldForeloebigProvision);
	}

	/**
	 * Metoder fremsøger den nyeste afregning (tid for oprettelse) for den aktuelle forsikring. Fremsøgningen sker via
	 * forsikringens dækninger, præmiefordringer, fordringer og derfra til afregningen.
	 * <p>
	 * Bemærk funktionen laver fremsøgningen for hvert kald og det er ikke muligt at gemme resultatet af søgningen idet
	 * nyeste afregning kan ændrer sig i objektets levetid.
	 * 
	 * @return Nyeste grund-<code>Afregning</code>, hvis ingen <code>null</code>.
	 * @since 2004-11-23 Gensafe Pro 1.3
	 */
	public Afregning getNyesteAfregning() {
		// SQL der laver samme søgning:
		// select distinct ar.udsnid, ar.oprkl, ar.oprdat
		// from frpr, produkt, fordrext, ar, xoaflbl
		// where produkt.ident2 = xoaflbl.ident and aflbl like '%217325%'
		// and produkt.ident = frpr.ident3
		// and frpr.ident2 = fordrext.ident
		// and fordrext.ident1 = ar.ident
		List<Afregning> result = new ArrayList<Afregning>();
		Produkt[] produkter = getDaekninger();

		for (int i = 0; produkter != null && i < produkter.length; i++) {
			FordringPraemie[] frpr = produkter[i].getFordringPraemie();

			for (int j = 0; frpr != null && j < frpr.length; j++) {
				Fordring fordring = frpr[j].getFordring();

				Afregning afregning = fordring.getAfregning();
				if (afregning != null && !result.contains(afregning)) {
					result.add(afregning);
				}
			}
		}

		if (result.size() == 0) {
			return null;
		}

		// Sorter i stigende orden
		Collections.sort(result, new ModelObjektIdComparator());
		return result.get(result.size() - 1);
	}
	
	/**
	 * Finder den nyeste grundafregning til aftalen, også uden at der findes præmie fordringer på afregningen.
	 * @since 17/11/2009 Gensafe Pro 2.2
	 */
	public Afregning getAfregningNyesteDirekte() {
		Afregning[] afregninger = getAfregning();
		if(afregninger != null && afregninger.length > 0) {
			Arrays.sort(afregninger, new ModelObjektIdComparator());
			return afregninger[afregninger.length - 1];
		}
		return null;
	}
	
	public Afregning getAfregningNyesteMedPraemieFordringer() {
		Afregning[] afregninger = getAfregning();
		if(afregninger != null && afregninger.length > 0) {
			Arrays.sort(afregninger, new ModelObjektIdComparator(true));
			// Nyeste ligger nu først og ældste sidst
			for (Afregning afregning : afregninger) {
	            Fordring[] fordringer = afregning.getAlleUnderliggendeAfregningersFordringer();
	            if (fordringer != null) {
	            	for (Fordring fordring : fordringer) {
	                    if (fordring.isPraemieFordring())
	                    	return afregning;
                    }
	            }
            }
		}
		return null;
	}
	public BigDecimal getAfregningNyestePraemieNetto() {
		Afregning afregning = getAfregningNyesteMedPraemieFordringer();
		BigDecimal svar = BigDecimal.ZERO;
		if (afregning != null) {
            Fordring[] fordringer = afregning.getAlleUnderliggendeAfregningersFordringer();
            if (fordringer != null) {
            	for (Fordring fordring : fordringer) {
                    if (fordring.isPraemieFordring()) {
                    	svar = svar.add(fordring.getBeloebFortegn());
                    }
                }
            }
		}
		return svar.negate();
	}
	/**
	 * 
	 * Hvis aftale har en topafregning med udestående, og denne afregning har en rykkerkonsekvens med betalingskonsekvens - panthaverbrev1 - 
	 * returneres true
	 * ellers
	 * returneres false
	 * 
	 */
	public boolean harUdsendtPanthaverbrev1() {
		Afregning[] afregninger = this.getTopAfregningerIkkeUdlignet();
		if(afregninger == null) return false;
		for (Afregning ar : afregninger) {
			Rykkerkonsekvens[] rkkoer = ar.getRykkerkonsekvensMedBetalingskonsekvens();
			if(rkkoer != null){
				for (Rykkerkonsekvens rkko : rkkoer) {
					if(rkko.isRykkerKonsekvensPanthaverbreve()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isUngBil(BigDecimal pDato) {
		Egenskabsgruppe ungbilGrp = DBServer.getInstance().getRegelServer().getGenstandsgrp(Genstandsegngrp.UNGBIL);
		if (ungbilGrp != null) {
			ArrayList<Genstand> genstandeGld = getGenstandeGld(pDato);
			if (genstandeGld != null) {
				for (Genstand genstand : genstandeGld) {
					Egenskab felt = genstand.getFelt(Genstandsegngrp.Genstandsfelt.UNGBIL, pDato);
					if (felt != null && felt.formatToDisplay().startsWith("Ja")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public Afregning[] getTopAfregningerIkkeUdlignet() {
		Afregning[] ar = getGrundAfregningerIkkeUdlignet();
		if (ar != null && ar.length > 0) {
			ArrayList<Afregning> result = new ArrayList<Afregning>();
			for (int i = 0; ar != null && i < ar.length; i++) {
				if (!result.contains(ar[i].getTopAfregning()))
					result.add(ar[i].getTopAfregning());
			}
			return ContainerUtil.toArray(result);
		}

		return null;
	}

	/**
	 * 
	 * @return forsikringens grundafregninger, der ikke er fuldt udlignet = har saldo
	 */
	public Afregning[] getGrundAfregningerIkkeUdlignet() {
		OQuery qry = QueryService.queryCreateTom();
		qry.add("N", "fuldtudlignetj_n", OQuery.EQUAL);
		return (Afregning[]) DBServer.getInstance().getVbsf().get(this, "Afregning", qry);
	}

	public BigDecimal[] findFremtidigeUdfoerteTariferingsDatoer(BigDecimal pDato) {
		OQuery qry = QueryService.queryCreate(DagbogProduktImpl.class);
		qry.add(GaiaConst.NULBD, "udfdato", OQuery.NOT_EQUAL);
		qry.add(pDato, "gld", OQuery.GREATER);
		qry.add(OpgavetypeImpl.getOpgavetype(OpgavetypeImpl.TARIFERING).getId(), "Opgavetype", OQuery.EQUAL);
		qry.addJoin(DagbogProduktImpl.class.getName() + ".Produkt", ProduktImpl.class.getName() + ".produktId");
		qry.add(aftaleId, ProduktImpl.class.getName() + ".Aftale");

		DagbogProdukt[] dgpd = (DagbogProdukt[]) DBServer.getInstance().getVbsf().queryExecute(qry);

		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		for (int i = 0; dgpd != null && i < dgpd.length; i++) {
			if (!result.contains(dgpd[i].getGld()))
				result.add(dgpd[i].getGld());
		}
		Collections.sort(result);
		return ContainerUtil.toArray(result);
	}

	// genererede accessmetoder til TaksatorBesigtigelseRekvisition
	public TaksatorBesigtigelseRekvisition[] getTaksatorBesigtigelseRekvisition() {
		return (TaksatorBesigtigelseRekvisition[]) DBServer.getInstance().getVbsf().get(this, "TaksatorBesigtigelseRekvisition");
	}

	public void addTaksatorBesigtigelseRekvisition(TaksatorBesigtigelseRekvisition pTaksatorBesigtigelseRekvisition) {
		PersistensService.addToCollection(this, "TaksatorBesigtigelseRekvisition", pTaksatorBesigtigelseRekvisition);
	}

	public void removeTaksatorBesigtigelseRekvisition(TaksatorBesigtigelseRekvisition oldTaksatorBesigtigelseRekvisition) {
		PersistensService.removeFromCollection(this, "TaksatorBesigtigelseRekvisition", oldTaksatorBesigtigelseRekvisition);
	}

	// genererede accessmetoder til RrTOrPd
	public RrTOrPd[] getRrTOrPd() {
		return (RrTOrPd[]) DBServer.getInstance().getVbsf().get(this, "RrTOrPd");
	}

	public void addRrTOrPd(RrTOrPd pRrTOrPd) {
		PersistensService.addToCollection(this, "RrTOrPd", pRrTOrPd);
	}

	public void removeRrTOrPd(RrTOrPd oldRrTOrPd) {
		PersistensService.removeFromCollection(this, "RrTOrPd", oldRrTOrPd);
	}

	/**
	 * Udskriv panthaverbrev "on-line"
	 * 
	 * @param pDato
	 * @since 28-02-2005 Gensafe Pro 1.3
	 */
	public void bestilPanthaverBrev(BigDecimal pDato, Genstand pGenstand) {
		Notesdokumenttype ndtp = NotesdokumenttypeImpl.loadNotesdokumenttype(Notesdokumenttype.PANTHAVER_VED_OPHOER);
		String pRetur = ndtp.genstandsUdskrift(pGenstand, pDato);
		log_.info("AftaleImpl.bestilPanthaverBrev() - NOAPPINT.PRETUR = " + pRetur);
	}

	// genererede accessmetoder til OpfoelgningCRMAfgang
	public OpfoelgningCRMAfgang[] getOpfoelgningCRMAfgang() {
		return (OpfoelgningCRMAfgang[]) DBServer.getInstance().getVbsf().get(this, "OpfoelgningCRMAfgang");
	}

	public void addOpfoelgningCRMAfgang(OpfoelgningCRMAfgang pOpfoelgningCRMAfgang) {
		PersistensService.addToCollection(this, "OpfoelgningCRMAfgang", pOpfoelgningCRMAfgang);
	}

	public void removeOpfoelgningCRMAfgang(OpfoelgningCRMAfgang oldOpfoelgningCRMAfgang) {
		PersistensService.removeFromCollection(this, "OpfoelgningCRMAfgang", oldOpfoelgningCRMAfgang);
	}

	// HOT-127727 Første værdi der beregnes skal anvendes hele vejen igennem
	// Ellers kan valid dato blive ignoreret af Revideringsbuffer
	// hvis der i mellemtiden er sat ophør
	// Initieres ved instantiering og af Revideringen
	private BigDecimal aeldsteAendringsDatoTilladt = null;
	private boolean beregnMedHensynTilOphoer = false;

	public BigDecimal getAeldsteAendringsDatoTilRevidering() {
		if (aeldsteAendringsDatoTilladt != null) {
			return aeldsteAendringsDatoTilladt;
		}
		return getAeldsteAendringsDatoTilladt();
	}
	public void nulstilAeldsteAendringsDatoTilRevidering() {
		aeldsteAendringsDatoTilladt = null;
		beregnMedHensynTilOphoer = false;
	}
	/**
	 * @inheritDoc
	 */
	public BigDecimal getAeldsteAendringsDatoTilladt() {
		if (aeldsteAendringsDatoTilladt == null) {
			if (this.isOphoert(Datobehandling.getDagsdatoBigD())) {
				beregnMedHensynTilOphoer = true;
			}
		}
		if (isKorttid()) {
			/**
			 * For korttidsforsikringer bruger vi en tillempet regel = seneste årsdag - 2 år.
			 *
			 * @since v1.5, 17.feb.2006 pk
			 */
			BigDecimal foedsdato = Datobehandling.getSenesteFoedselsdag(this.getGld());
			if (foedsdato == null) {
				aeldsteAendringsDatoTilladt = this.getGld();
			}
			else {
				aeldsteAendringsDatoTilladt = Datobehandling.datoPlusMinusAntalAar(foedsdato, -2);
			}
		}
		else {
			// HOT-81483 Hvis ophør er passeret er dagsdato udgangspunktet - ikke HF
			if (beregnMedHensynTilOphoer) {
				aeldsteAendringsDatoTilladt = Datobehandling.datoPlusMinusAntalAar(Datobehandling.getDagsdatoBigD(), -2);
			}
			else {
				aeldsteAendringsDatoTilladt = Datobehandling.datoPlusMinusAntalAar(findSenesteHovedForfaldsDato(Datobehandling.getDagsdatoBigD()), -2);
			}

            if (getAftaleTypen().isSkadebehandlingsforsikring()) {
				aeldsteAendringsDatoTilladt = Datobehandling.datoPlusMinusAntalAar(aeldsteAendringsDatoTilladt, -8);
            }
			aeldsteAendringsDatoTilladt = aeldsteAendringsDatoTilladt.compareTo(this.getGld()) < 0 ? this.getGld() : aeldsteAendringsDatoTilladt;
		}

		return aeldsteAendringsDatoTilladt;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isAendringTilladtStopklods(BigDecimal pDato) {
		if (pDato == null)
			return false;
		return pDato.compareTo(this.getAeldsteAendringsDatoTilladt()) >= 0;
	}
	
	/**
	 * 
	 * @return den ældste tilladte nytegningsdato d.d.
	 */
	public static BigDecimal getAeldsteNytegningTilladtStopklods() {
// Fravalgt fordi det er muligt at tegne en aftale der efterfølgende ikke kan ændres -- 
// hvis man vælger HF <> Tegningsmåned -- begge i tidligere måned end indeværende
		BigDecimal foersteIMaaned = Datobehandling.getDenFoerste(Datobehandling.getDagsdatoBigD());
		return Datobehandling.datoPlusMinusAntalAar(foersteIMaaned, -2);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAendringTilladtKorttid(BigDecimal pDato) {
		if (!this.isKorttid()) {
			return true;
		}

		if (pDato == null) {
			return false;
		}

		Genstand[] gnTab = getGenstand();
		if (gnTab == null || gnTab.length == 0) {
			return pDato.compareTo(this.getGld()) == 0;
		}
		for (Genstand gn : gnTab) {
			if (!gn.isAnnulleret() && pDato.compareTo(gn.getGld()) == 0) {
				return true;
			}
		}

		return false;
	}
	
	public BigDecimal getSenesteAendringsdatoTilladtKorttid() {
		if (!this.isKorttid()) {
			return null;
		}

		BigDecimal rtnDato = BigDecimal.ZERO;

		Genstand[] gnTab = getGenstand();
		if (gnTab == null || gnTab.length == 0) {
			return this.getGld();
		}
		for (Genstand gn : gnTab) {
			if (!gn.isAnnulleret() && rtnDato.compareTo(gn.getGld()) < 0) {
				rtnDato = gn.getGld();
			}
		}

		return rtnDato;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isElektroniskOverfoert() {
		Egenskab elektroniskOverfoert = EgenskabHolderImpl.getEgenskab(this, Aftaleegngrp.NE_ELEKTRONISK_OVERFØRT);
		// Returnerer "Ja" eller "Nej".
		if (elektroniskOverfoert != null && elektroniskOverfoert.getBenaevnelse().trim().startsWith(GaiaConst.JA)) {
			return true;
		}
		return false;
	}
	
	public boolean isUdstedelsesAarsag(UdstedelsesAarsag udstedelsesAarsag, BigDecimal pDato) {
		Egenskab udstedelsesaarsag = EgenskabHolderImpl.getEgenskab(this, Aftaleegngrp.UDSTAARSAG, pDato);
		return udstedelsesaarsag != null && udstedelsesaarsag.getBenaevnelse().trim().equals(udstedelsesAarsag.bnv);
    }

	/**
	 * @inheritDoc
	 */
	public BigDecimal getGldOprindelig() {
		Egenskab eg = EgenskabHolderImpl.getEgenskab(this, GensamUtil.fixLength(Aftaleegngrp.OPRINDELIG_IKRAFT, 10));
		// TODO spild af tid at bruge fixlength og bør i øvrigt bruge getFelt(grp);
		if (eg != null) {
			String ddmmyyyy = eg.getBenaevnelse().trim();
			try {
				return Datobehandling.skaermDatoTilBigDecimal(ddmmyyyy, "ddMMyyyy");
			} catch (ParseException e) {
				// datafejl i egenskaben - teoretisk
			} catch (NumberFormatException e) {
				// datafejl i egenskaben - teoretisk
			}
		}

		return this.getGld();
	}

	/**
	 * returnerer true, hvis et af aftalens områder er det samme som medsendt område fra RrTOr
	 * 
	 * @param pRrTOr
	 * @return boolean
	 * @since 09/10/2006 Gensafe Pro 1.6
	 */
	public boolean isPrimaerRisikoOmraade(RrTOr pRrTOr) {
		if (!pRrTOr.getReaRisikoTp().isOmraadefoelsom()) {
			return true;
		}
		RrTOrAf[] rrTOrAf = this.getRrTOrAf(pRrTOr.getGld());
		for (int ra = 0; rrTOrAf != null && ra < rrTOrAf.length; ra++) {
			if (rrTOrAf[ra].getReaRisikoTp().equals(pRrTOr.getReaRisikoTp())) {
				Omraade omrade = rrTOrAf[ra].getOmraade();
				if (omrade != null && omrade.equals(pRrTOr.getOmraade())) { /* npe-check juli07 */
					return true;
				}
			}
		}
		return false;
	}
	
	 /**
     * 
     * @return nyeste skadeblokering for aftale (set ud fra gældende på skadeblokering)
     * 
     */
    public SkadeBlokering getSkadeBlokeringNyeste() {

        SkadeBlokering skadeBlokeringNyeste = null;

        SkadeBlokering[] skadeBlokeringer = this.getSkadeBlokering();
        for (int i = 0; skadeBlokeringer != null && i < skadeBlokeringer.length; i++) {
            if ((skadeBlokeringNyeste == null || skadeBlokeringer[i].getGld().compareTo(skadeBlokeringNyeste.getGld()) > 0)) {
                skadeBlokeringNyeste = skadeBlokeringer[i];
            }
        }
        
        return skadeBlokeringNyeste;
    }

	// genererede accessmetoder til Gebyr
	public Gebyr[] getGebyr() {
		return (Gebyr[]) DBServer.getInstance().getVbsf().get(this, "Gebyr");
	}

	public void addGebyr(Gebyr pGebyr) {
		PersistensService.addToCollection(this, "Gebyr", pGebyr);
	}

	public void removeGebyr(Gebyr oldGebyr) {
		PersistensService.removeFromCollection(this, "Gebyr", oldGebyr);
	}
	
	public void markGebyrCollectionDirty() {
		DBServer.getInstance().getVbsf().markCollectionDirty(this, "Gebyr");
	}
	
	public Gebyr[] getGebyrIkkeUdlagtTilFordring() {
		OQuery qry = QueryService.queryCreate(GebyrImpl.class);
		qry.add(getId(), "Aftale", OQuery.EQUAL);
		qry.add(GaiaConst.TOMSTRING, "Fordring");
		return (Gebyr[]) DBServer.getInstance().getVbsf().queryExecute(qry);
	}
	

	/**
	 * Implementation of {@link Aftale#isGeneralPolice()}
	 */
	public boolean isGeneralPolice() {
		return getAftaletypeKortBenaevnelse().trim().equals(GENERAL_POLICE_KORT_BENEVNELSE);
	}

	@Override
	public boolean hasAftaleRegDatoAfregning(Afregning pAfregning) {
		if (pAfregning != null) {
			AftaleRegDatoAfregning[] rels = (AftaleRegDatoAfregning[]) DBServer.getInstance().getVbsf().get(this, "AftaleRegDatoAfregning");
			if (rels != null) {
				for (AftaleRegDatoAfregning rel : rels) {
		        	if (rel.getAfregning() != null && rel.getAfregning().equals(pAfregning))
		        		return true;
		        }
			}
		}
		return false;
	}

	public List<AftaleRegDatoAfregning> getAftaleRegDatoAfregningAabne() {
		List<AftaleRegDatoAfregning> result = new ArrayList<AftaleRegDatoAfregning>();
		AftaleRegDatoAfregning[] aftaleRegDatoAfregningTab = getAftaleRegDatoAfregning();
		if (aftaleRegDatoAfregningTab != null) {
			for (AftaleRegDatoAfregning aftaleRegDatoAfregning : aftaleRegDatoAfregningTab) {
				if (!(aftaleRegDatoAfregning.hasAfregning())) {
					result.add(aftaleRegDatoAfregning);
				}
			}
		}
		return result;
	}
	// genererede accessmetoder til AftaleRegDatoAfregning
	
	public AftaleRegDatoAfregning[] getAftaleRegDatoAfregning() {
		return (AftaleRegDatoAfregning[]) DBServer.getInstance().getVbsf().get(this, "AftaleRegDatoAfregning");
	}

	public void addAftaleRegDatoAfregning(AftaleRegDatoAfregning pAftaleRegDatoAfregning) {
		PersistensService.addToCollection(this, "AftaleRegDatoAfregning", pAftaleRegDatoAfregning);
	}

	public void removeAftaleRegDatoAfregning(AftaleRegDatoAfregning oldAftaleRegDatoAfregning) {
		PersistensService.removeFromCollection(this, "AftaleRegDatoAfregning", oldAftaleRegDatoAfregning);
	}

	// genererede accessmetoder til RykkerkonsekvensOmfang
	public RykkerkonsekvensOmfang[] getRykkerkonsekvensOmfang() {
		return (RykkerkonsekvensOmfang[]) DBServer.getInstance().getVbsf().get(this, "RykkerkonsekvensOmfang");
	}

	public void addRykkerkonsekvensOmfang(RykkerkonsekvensOmfang pRykkerkonsekvensOmfang) {
		PersistensService.addToCollection(this, "RykkerkonsekvensOmfang", pRykkerkonsekvensOmfang);
	}

	public void removeRykkerkonsekvensOmfang(RykkerkonsekvensOmfang oldRykkerkonsekvensOmfang) {
		PersistensService.removeFromCollection(this, "RykkerkonsekvensOmfang", oldRykkerkonsekvensOmfang);
	}

	// genererede accessmetoder til SkadeBlokering
	public SkadeBlokering[] getSkadeBlokering() {
		return (SkadeBlokering[]) DBServer.getInstance().getVbsf().get(this, "SkadeBlokering");
	}

	public void addSkadeBlokering(SkadeBlokering pSkadeBlokering) {
		PersistensService.addToCollection(this, "SkadeBlokering", pSkadeBlokering);
	}

	public void removeSkadeBlokering(SkadeBlokering oldSkadeBlokering) {
		PersistensService.removeFromCollection(this, "SkadeBlokering", oldSkadeBlokering);
	}
	
	public boolean isSkadeblokeret(BigDecimal enDato) {
		SkadeBlokering[] skadeBlokeringer = (SkadeBlokering[])Datobehandling.findGaeldende(getSkadeBlokering(), enDato);
		if (skadeBlokeringer!=null && skadeBlokeringer.length>0) {
			return true;
		}
		return false;
	}

	// genererede accessmetoder til Overblik hændelse manuel
	public void addOverblikHaendelseManuel(OverblikHaendelseManuel pOverblikHaendelseManuel) {
		PersistensService.addToCollection(this, "OverblikHaendelseManuel", pOverblikHaendelseManuel);
	}

	public OverblikHaendelseManuel[] getOverblikHaendelseManuel() {
		return (OverblikHaendelseManuel[]) DBServer.getInstance().getVbsf().get(this, "OverblikHaendelseManuel");
	}

	public void removeOverblikHaendelseManuel(OverblikHaendelseManuel oldOverblikHaendelseManuel) {
		PersistensService.removeFromCollection(this, "OverblikHaendelseManuel", oldOverblikHaendelseManuel);
	}

	public BigDecimal getFiktivProduktOmkostningBeloeb(BigDecimal pDato) {
		BigDecimal beloeb = GaiaConst.NULBD;
		Produkt[] pdTab = this.getDaekningerGld(pDato);
		if (pdTab != null) {
			for (int i = 0; i < pdTab.length; i++) {
				FiktivProduktOmkostningstype[] fktPdom = pdTab[i].getFiktivProduktOmkostningstype();
				if (fktPdom != null) {
					for (int j = 0; j < fktPdom.length; j++) {
						beloeb = beloeb.add(fktPdom[j].getOmkostningsbeloeb());
					}
				}
			}

		}
		return beloeb;

	}

	public BigDecimal getSenesteUdfoertBonusreguleringsdato(BigDecimal pDato) {
		DagbogAftale dgaf = getSenesteDagbogAftaleFoer(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), true, pDato);
		if (dgaf != null) {
			return dgaf.getGld();
		}
		return null;
	}

	public BigDecimal getSenesteBonusreguleringsdatoFoer(BigDecimal pDato) {
		DagbogAftale dgaf = getSenesteDagbogAftaleFoer(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), null, pDato);
		if (dgaf != null) {
			return dgaf.getGld();
		}
		return null;
	}

	public BigDecimal getFoerstKommendeBonusreguleringsdato(BigDecimal pDato) {
		DagbogAftale dgaf = getFoerstKommendeDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), null, pDato);
		if (dgaf != null) {
			return dgaf.getGld();
		}
		return null;
	}
	
	public BigDecimal getFoerstKommendeBonusreguleringsdatoEjUdfoert(BigDecimal pDato) {
		DagbogAftale dgaf = getFoerstKommendeDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), false, pDato);
		if (dgaf != null) {
			return dgaf.getGld();
		}
		return null;
	}
	
	public boolean findesUdfoerteBonusreguleringerEfter(BigDecimal pDato) {
		BigDecimal tmpDato = Datobehandling.datoPlusMinusAntal(pDato, 1);
		DagbogAftale dgaf = getFoerstKommendeDagbogAftale(OpgavetypeImpl.getOpgavetype(Opgavetype.BONUSREG), true, tmpDato);
		if (dgaf != null) {
			return true;
		}
		return false;
	}

	public BigDecimal getSenesteBonusbelastendeSkade(Periode pPeriode) {
		BigDecimal result = null;
		Skadesag[] skadesager = getSkadesager(pPeriode);
		for (int i = 0; skadesager != null && i < skadesager.length; i++) {
			Boolean bonusbelastende = skadesager[i].isBonusbelastende();
			if (bonusbelastende != null && bonusbelastende && (result == null || skadesager[i].getGld().compareTo(result) > 0)) {
				result = skadesager[i].getGld();
			}
		}
		return result;
	}

	@Override
	public int getAntalIkkeBehandledeBonusbelastendeSkader() {
		int antalSkader = getSkadesagerBonusbelastendeAntal();
		if (DBServer.getInstance().getSelskabsOplysning().medtagFlyttetFraForsikringVedBonusregulering()) {
			Aftale flyttetFraAftale = null;
			if (isTilAftale()) {
				AftaleFlytningLog[] aftaleFlytningLog = getAftaleFlytningLogTilAftale();
				if (aftaleFlytningLog != null && aftaleFlytningLog.length > 0) {
					flyttetFraAftale = aftaleFlytningLog[0].getFraAftale();
				}
			}
			if (isGenoptagetTilAftale()) {
				AftaleGenoptagLog[] aftaleGenoptagLog = getAftaleGenoptagLogTilAftale();
				if (aftaleGenoptagLog != null && aftaleGenoptagLog.length > 0) {
					flyttetFraAftale = aftaleGenoptagLog[0].getFraAftale();
				}
			}
			if (flyttetFraAftale != null) {
				antalSkader = antalSkader + flyttetFraAftale.getSkadesagerBonusbelastendeAntal();
			}
		}
		return antalSkader;
	}

	public void setStatusKoerselMaxAfvDage(boolean pStatusKoerselMaxAfvDage) {
		statusKoerselMaxAfvDage = pStatusKoerselMaxAfvDage;
	}

	public boolean getStatusKoerselMaxAfvDage() {
		return statusKoerselMaxAfvDage;
	}

	public int getRestanceStatus() {
		Afregning[] afregninger = getAfregning();

		if (afregninger == null) {
			return Aftale.RESTANCESTATUS_HVID;
		}
		BigDecimal saldo = BigDecimal.ZERO;
		for (int i = 0; i < afregninger.length; i++) {
			saldo = saldo.add(afregninger[i].getSaldo());
		}
		if (saldo.doubleValue() <= 0) {
			return Aftale.RESTANCESTATUS_HVID;
		}
		
		for (int i = 0; i < afregninger.length; i++) {
			Afregning afregning = afregninger[i];
			Afregning topAfregning = afregning.getTopAfregning();
			if (!afregning.isUdlignet() && topAfregning.isRykket()) {
				return Aftale.RESTANCESTATUS_ROED;
			}		
		}
		
		BigDecimal dagsDato = Datobehandling.getDagsdatoBigD();
		for (int i = 0; i < afregninger.length; i++) {
			Afregning afregning = afregninger[i];
			Afregning topAfregning = afregning.getTopAfregning();
			if (!afregning.isUdlignet() && topAfregning.getBetalingsfrist().compareTo(dagsDato) < 0) {
				return Aftale.RESTANCESTATUS_GUL;
			}		
		}

		return Aftale.RESTANCESTATUS_GROEN;
	}

	// genererede accessmetoder til ReguleringsSkema
	public ReguleringsskemaStatus[] getReguleringsSkema() {
		return (ReguleringsskemaStatus[]) DBServer.getInstance().getVbsf().get(this, "ReguleringsSkema");
	}

	public void addReguleringsSkema(ReguleringsskemaStatus pReguleringsSkema) {
		PersistensService.addToCollection(this, "ReguleringsSkema", pReguleringsSkema);
	}

	public void removeReguleringsSkema(ReguleringsskemaStatus oldReguleringsSkema) {
		PersistensService.removeFromCollection(this, "ReguleringsSkema", oldReguleringsSkema);
	}

	/**
	 *
	 * @param pTilDato
	 * @param pMedtagKonverterede
	 * @param typeFilter null=alle, ellers skal det være et NOT_IN-format
	 * @return kampklar OQuery
	 */
	public static OQuery getKorttidsOQuery(BigDecimal pTilDato, boolean pMedtagKonverterede, String typeFilter) {
//			String clsAftaleStatustype = AftaleStatustypeImpl.class.getName();
//			String clsStatustype = StatustypeImpl.class.getName();
//			String clsAftaletype = AftaletypeImpl.class.getName();
			String clsAftale = AftaleImpl.class.getName();
			String clsAfafeg = AftaleAfegnImpl.class.getName();
			String clsAfeggp = AftaleegngrpImpl.class.getName();

			OQuery qry = DBServer.getInstance().getVbsf().queryCreate(AftaleImpl.class);
			qry.add(BigDecimal.ZERO, "oph", OQuery.GREATER);
			qry.add(pTilDato, "oph", OQuery.LESS);
			// Dette ville være guld men understøttes ikke af vbsf hvis der joines.
			//qry.add((String)null, "getOphAttrName() >= getGldAttrName()", OQuery.INSERT_TEXT, OQuery.AND);
			
			qry.addJoin(clsAftale + ".aftaleId", clsAfafeg + ".EgenskabHolder");
			qry.addJoin(clsAfafeg + ".Egenskabsgruppe", clsAfeggp + ".aftaleegngrpId");
			qry.add(Aftaleegngrp.AARSBASERET_PRAEMIE, clsAfeggp + ".kortBenaevnelse");
			qry.add("N", clsAfafeg + ".benaevnelse", OQuery.EQUAL);
					
			String kortbenaevnelser = "(STTP.KRTBNV = '" + Statustype.OPHOERT + "'";
			if (!pMedtagKonverterede) {
				kortbenaevnelser += " OR STTP.KRTBNV = '" + "KONVLUK" + "'";
			}
			kortbenaevnelser += ")";
			String SQLOPH = "SELECT AFSTTP.IDENT FROM afsttp, sttp WHERE AFSTTP.IDENT2= " + " STTP.IDENT and AFSTTP.OPHDAT = 0 and "
			        + kortbenaevnelser;
			
			qry.add(SQLOPH, "aftaleId", OQuery.NOT_IN);

			if (typeFilter != null) {
				qry.add(typeFilter, "typeId", OQuery.NOT_IN);
			}

			qry.addOrder("oph"); // ældste ophør øverst
			//qry.setMaxCount(7001);
			return qry;
	 }
	
	public boolean isFoersteForsikring(Individ pProvisionsmodtager) {
		String clsAftale          = AftaleImpl.class.getName();
		String clsAftalehaendelse = AftalehaendelseImpl.class.getName();
		String clsFtAfhnPt        = ForsikringstagerAftalehaendelseProvisionmodtagerImpl.class.getName();
		
		OQuery qry = DBServer.getVbsfInst().queryCreate(AftaleImpl.class);
		qry.add(getTegnesAfIndividId(), "TegnesAfIndivid",OQuery.EQUAL);
		qry.add(pProvisionsmodtager.getId(), clsFtAfhnPt + ".Provisionsmodtager", OQuery.EQUAL);
		qry.addJoin(clsAftale + ".aftaleId", clsAftalehaendelse + ".Aftale");
		qry.addJoin(clsAftalehaendelse + ".aftalehaendelseId", clsFtAfhnPt + ".Aftalehaendelse");
		qry.addOrder("oprDato");
		qry.addOrder("oprTid");
		qry.setDistinct(true);
		
		Aftale[] aftaler = (Aftale[]) DBServer.getVbsfInst().queryExecute(qry);
		// OK finder så den første forsikring hvor provisionsmodtager er tilknyttet pr. forsikringens ikrafttrædelsesdato.
		Aftale nyesteAftale = null;
		boolean provisionsmodtagerFundet = false;
		for (int i = 0; aftaler != null && !provisionsmodtagerFundet && i < aftaler.length; i++) {
			if(!aftaler[i].isAnnulleret()) {
				Aftalehaendelse afhn = aftaler[i].getAftalehaendelseNyeste(aftaler[i].getGld(), true);
				ForsikringstagerAftalehaendelseProvisionmodtager[] ftafhnpt = afhn.getForsikringstagerAftalehaendelseProvisionmodtager();
				for (int j = 0; ftafhnpt != null && j < ftafhnpt.length; j++) {
					if(ftafhnpt[j].getProvisionsmodtager().equals(pProvisionsmodtager)) {
						nyesteAftale = aftaler[i];
						provisionsmodtagerFundet = true;
					}
				}
			}
		}
		if(!provisionsmodtagerFundet || this.equals(nyesteAftale)) {
			return true;
		}
		return false;
	}

	
	public ForsikringstagerAftalehaendelseProvisionmodtager getProvisionsmodtagerPaaForsikring(BigDecimal pSekvens) {
		OQuery qry = DBServer.getInstance().getVbsf().queryCreateTom();
		qry.add(GaiaConst.TOMSTRING, "Aftalehaendelse",OQuery.EQUAL);
		qry.add(GaiaConst.TOMSTRING, "Forsikringstager",OQuery.EQUAL);
		if(pSekvens.intValue() >= 0) {
			qry.add(pSekvens, "sekvens", OQuery.EQUAL);
		}

		ForsikringstagerAftalehaendelseProvisionmodtager[] faper = (ForsikringstagerAftalehaendelseProvisionmodtager[]) DBServer.getInstance().getVbsf().get(this, "ForsikringstagerAftalehaendelseProvisionmodtager", qry);
		if(faper != null && faper.length > 0) {
			return faper[0];
		}
		return null;
	}
	
	public boolean findesProvisionsmodtagerPaaForsikringsNiveau() {
		return getProvisionsmodtagerPaaForsikring(new BigDecimal("-1")) != null;
	}

	public ForsikringstagerAftalehaendelseProvisionmodtager[] getForsikringstagerAftalehaendelseProvisionmodtager() {
		return (ForsikringstagerAftalehaendelseProvisionmodtager[]) DBServer.getInstance().getVbsf().get(this, "ForsikringstagerAftalehaendelseProvisionmodtager");
	}

	public void addForsikringstagerAftalehaendelseProvisionmodtager(
			ForsikringstagerAftalehaendelseProvisionmodtager pForsikringstagerAftalehaendelseProvisionmodtager) {
		PersistensService.addToCollection(this, "ForsikringstagerAftalehaendelseProvisionmodtager", pForsikringstagerAftalehaendelseProvisionmodtager);
		
	}

	public void removeForsikringstagerAftalehaendelseProvisionmodtager(
			ForsikringstagerAftalehaendelseProvisionmodtager oldForsikringstagerAftalehaendelseProvisionmodtager) {
		PersistensService.removeFromCollection(this, "ForsikringstagerAftalehaendelseProvisionmodtager", oldForsikringstagerAftalehaendelseProvisionmodtager);

	}

	@Override
	public boolean isAccepteretTilbud() {
		if (!isTilbud())
			return false;

		AftaleStatustype aftaleStatustype = getAftaleStatustypeNyesteUdenOphoer();
		return aftaleStatustype != null && aftaleStatustype.getStatustype().isTilbudAccepteret();
	}

	public boolean isDoedTilbud() {
		if (!isTilbud())
			return false;

	   	AftaleStatustype aftaleStatustype = getAftaleStatustypeNyesteUdenOphoer();
    	return aftaleStatustype != null && aftaleStatustype.getStatustype().isDoedTilbud();
	}

	public boolean isTilbudGodkendt() {
		AftaleStatustype aftaleStatustype = getAftaleStatustypeNyesteUdenOphoer();
		String status = aftaleStatustype == null ? "" : aftaleStatustype.getStatustype().getKortBenaevnelse().trim();
		return status.equals(Statustype.STATUS_TYPE_TILBUD_DOED_EFTER_GODKENDT);
	}
	
	public void setUndladKorttidsRevidering(boolean pUndladKorttidsRevidering) {
		undladKorttidsRevidering = pUndladKorttidsRevidering;
	}
	
	public boolean isUndladKorttidsRevidering() {
		return undladKorttidsRevidering;
	}
	
	public void afslutForkortKorttid(BigDecimal pOrgOph) {
		BigDecimal maximaltAntalAarDerFindesReglerTil = RistornoRegelImpl.getMaximaltAntalAarDerFindesReglerTil();
		if (maximaltAntalAarDerFindesReglerTil == null)
			return;
		
		BigDecimal ristDato = Datobehandling.datoPlusMinusAntal(getOph(), 1);
		// Find alle præmiefordringer der opstod som følge af forkortelsen
		Afregning nyAr = getNyesteAfregning();
		if (nyAr == null) {
			return;
		}
		Fordring[] nyArFrTab = nyAr.getFordring();
		ArrayList<Fordring> ristFr = new ArrayList<Fordring>();
		BigDecimal totRistBlb = BigDecimal.ZERO; // Kun til spc. ristornoregler
		for (Fordring fr : nyArFrTab) {
			if (fr.getPeriodeFra().compareTo(ristDato) == 0 && fr.getFordringstype().isPraemie()) {
				ristFr.add(fr);
				totRistBlb = totRistBlb.add(fr.getNettobeloeb());
			}
		}
		if (ristFr.size() == 0 || totRistBlb.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		// Her skal beregnes evt. korrektionsprocent hvis der skal udbetales til kunden.
		BigDecimal korrFakt = BigDecimal.ONE;
		BigDecimal totTgn = Datobehandling.antalHeleAar(getGld(), pOrgOph).add(BigDecimal.ONE);
		if (totTgn.compareTo(maximaltAntalAarDerFindesReglerTil) > 0) {
			totTgn = maximaltAntalAarDerFindesReglerTil;
		}
		BigDecimal risAar = Datobehandling.antalHeleAar(getGld(), getOph()).add(BigDecimal.ONE);
		RistornoRegel risRgl = getAftaleTypen().getRistornoRegel(getGld(), totTgn.intValue(), risAar.intValue());
		if (risRgl != null) {
			BigDecimal totPrmBlb = BigDecimal.ZERO;
			Afregning[] alleAr = getAfregning();
			for (Afregning ar : alleAr) {
				if (ar.isGrundAfregning()) {
					Fordring[] alleFr = ar.getFordring();
					for (Fordring fr : alleFr) {
						if (fr.getFordringstype().isPraemie() && !(ristFr.contains(fr))) {
							totPrmBlb = totPrmBlb.add(fr.getNettobeloeb());
						}
					}
				}
			}
			if (totPrmBlb.compareTo(BigDecimal.ZERO) > 0) { // Bør altid være opfyldt, men ...
				BigDecimal ristTilKunde = totPrmBlb.multiply(risRgl.getRistornoprocentangivelse().divide(new BigDecimal(100)));
				korrFakt = BigDecimal.ONE.subtract(ristTilKunde.divide(totRistBlb.abs(), 3, RoundingMode.HALF_UP));
				if (korrFakt.compareTo(BigDecimal.ZERO) < 0) { // Der er grænser for galskaben
					korrFakt = BigDecimal.ONE;
				}
			}
		}
		
		// Modposter pr. ophørsdatoen så der ikke afsættes ny reserve m.m.
		boolean harSelvStartetTransaction = PersistensService.transactionBegin();
		ArrayList<Fordring> modpFr = new ArrayList<Fordring>();
		ArrayList<ReasFordringWorkModel> rfwml = new ArrayList<ReasFordringWorkModel>();
		for (Fordring fr : ristFr) {
			Fordring nyFr = fr.createNewFrom();
			nyFr.setBeloeb(nyFr.getBeloeb().negate().multiply(korrFakt).divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP));
			nyFr.setPeriodeFra(getOph());
			nyFr.setPeriodeTil(getOph());
			nyFr.setNettobeloeb(nyFr.getNettobeloeb().negate().multiply(korrFakt).divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP));
			((FordringImpl)nyFr).Afregning = GensamUtil.TOMSTRING;
			PersistensService.save(nyFr);
			modpFr.add(nyFr);
			// Gem til reassurance
			ReasFordringWorkModel rfwm = new ReasFordringWorkModel(nyFr.getProdukt(), nyFr.getPeriodeFra(), nyFr.getPeriodeTil());
			int ix = rfwml.indexOf(rfwm);
			if (ix < 0) {
				rfwml.add(rfwm);
				ix = rfwml.indexOf(rfwm);
			}
			rfwml.get(ix).addPraemieBeloeb(nyFr.getNettobeloeb());
			FordringRelation nyFrRel = fr.getFordringRelation().createNewFrom();
			nyFrRel.setPeriodefra(getOph());
			nyFrRel.setPeriodetil(getOph());
			((ModelObjekt)nyFrRel).setGld(getOph());
			nyFrRel.setFordring(nyFr);
			PersistensService.save(nyFrRel);
			nyFrRel.addToFordring(nyFr);
		}
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
		// Dan afregning og saml med ristornoen
		harSelvStartetTransaction = PersistensService.transactionBegin();
		Afregning modpAr[] = AfregningImpl.danAfregning((ContainerUtil.toArray(modpFr)), AfregningstypeImpl.getGIROAfregningstype(), 
													this, null, null, null, getOph(), nyAr.getBetalingsfrist(), 
													nyAr.getBetalingsfrist(), nyAr.getUdsendelsesdato(), false);
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
		harSelvStartetTransaction = PersistensService.transactionBegin();
		for (Fordring fr : ristFr) {
			fr.setAfregning(modpAr[0]);
			PersistensService.save(fr);
		}
		ArrayList<Afregning> alleArLst = (ArrayList<Afregning>)ContainerUtil.asList(modpAr);
		alleArLst.add(nyAr);
		Afregning[] alleAr = ContainerUtil.toArray(alleArLst);
		Afregning samlAr = AfregningManager.getInstance().samlAfregninger(alleAr, 
														AfregningManager.getInstance().findSalmeAfregningsform(alleAr), 
														(Afregningstype)null);
		if (risRgl != null) {
			// Policen skal ud sammen med checken men skrives nu
			bestilPoliceUdskrift(getOph());
		}
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
		// Reas
		harSelvStartetTransaction = PersistensService.transactionBegin();
		ReassuranceBeregningStyr rbs = new ReassuranceBeregningStyr();
		ReasFordringWorkPersistenceService rfwps = new ReasFordringWorkPersistenceService();
		ReasBeregnRegisterPersistenceService rbrps = new ReasBeregnRegisterPersistenceService();
		rfwps.save(rfwml);
		for (ReasFordringWorkModel rfwm : rfwml) {
			rbrps.udlaegReasBeregnRegister(rfwm.getDaekning().getAftale(), 
					rfwm.getDaekning(), null, rfwm.getPeriodeFra(),
					ReasBeregnRegister.PRODUKT_VEDLIGEHOLD);
		}
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
		harSelvStartetTransaction = PersistensService.transactionBegin();
		KaldReassurance kr = new KaldReassurance();
        kr.setAftale(rfwml.get(0).getDaekning().getAftale());
        kr.setPAendringsdato(rfwml.get(0).getPeriodeFra());
        kr.run();
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
	}
	
	public BigDecimal getAarspraemieInklStatsafgift(BigDecimal pDato) {
		Produkt[] produkter = getDaekningerGld(pDato);
		BigDecimal aarspraemie = BigDecimal.ZERO;
		if (produkter != null) {
			for (Produkt produkt : produkter) {
				aarspraemie = aarspraemie.add(produkt.getPraemieInklStatsafgift(pDato));
			}
		}
		return aarspraemie;
	}
	
	public BigDecimal getAarspraemieEksklusiveStatsafgift(BigDecimal pDato) {
		Produkt[] produkter = getDaekningerGld(pDato);
		BigDecimal aarspraemie = BigDecimal.ZERO;
		if (produkter != null) {
			for (Produkt produkt : produkter) {
				aarspraemie = aarspraemie.add(produkt.getPraemieNettoEksklusiveStatsafgift(pDato));
			}
		}
		return aarspraemie;
	}

	public BigDecimal getAarspraemieInklStatsafgiftAfrundet(BigDecimal pDato) {
		Produkt[] daekninger = getDaekningerGld(pDato);
		BigDecimal result = BigDecimal.ZERO;
		if(daekninger != null) {
			for (Produkt daekning : daekninger) {
				BigDecimal aarspraemieAfrundet = daekning.getAarspraemieAfrundet(pDato);
				if(aarspraemieAfrundet != null) {
					result = result.add(aarspraemieAfrundet);
				}
				BigDecimal statsafgiftAfrundet = daekning.getStatsafgiftAfrundet(pDato);
				if(statsafgiftAfrundet != null) {
					result = result.add(statsafgiftAfrundet);
				}
				
			}
		}
		return result;
	}
	
	/**
	 * Returnerer alle aftaler (forsikringer) svarende til et gammelt policenummer på den specifik dato.
	 * 
	 * @param pPolicenummer
	 *            Policenummer
	 * @param pDato
	 *            Datoen hvor aftalen (forsikringen) skal være gældende
	 * @return Aftalerne (forsikringerne) svarende til det gl. policenummer. Er null hvis der ikke kunne finds nogen aftaler
	 */
	public static Aftale[] getAftalerGlPolicenr(String pPolicenummer, BigDecimal pDato) {
		Egenskabsgruppe polnrGrp = EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.POLICENRGL, AftaleegngrpImpl.class, OQuery.EQUAL);
		Egenskab polnrE = polnrGrp.getEgenskab(pPolicenummer);
		if (polnrE == null){
			return null;
		}
		List<EgenskabHolder> aftaler = polnrE.getEgenskabHoldere(null); // null=relationer uden ophør 
		if (aftaler != null && !aftaler.isEmpty()){
			List<Aftale> retur = new ArrayList<Aftale>(aftaler.size());
			for (EgenskabHolder aftale : aftaler) {
				if (!aftale.isOphoert(pDato) && !aftale.isAnnulleret()){
					retur.add((Aftale)aftale);
				}
			}
			if (!retur.isEmpty()){
				return ContainerUtil.toArray(retur);
			}
		}
		return null;
		
	}
	
	/**
	 * Returnerer alle aftaler (forsikringer) svarende til et policenummer på den specifik dato.
	 * 
	 * @param pPolicenummer
	 *            Policenummer
	 * @param pDato
	 *            Datoen hvor aftalen (forsikringen) skal være gældende
	 * @return Aftalerne (forsikringerne) svarende til policenummeret. Er null hvis der ikke kunne finds nogen aftaler
	 */
	public static Aftale[] getAftaler(String pPolicenummer, BigDecimal pDato) {
		Egenskabsgruppe polnrGrp = EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.POLICENR, AftaleegngrpImpl.class, OQuery.EQUAL);
		Egenskab polnrE = polnrGrp.getEgenskab(pPolicenummer);
		if (polnrE == null){
			return null;
		}
		List<EgenskabHolder> aftaler = polnrE.getEgenskabHoldere(null); // null=relationer uden ophør 
		if (aftaler != null && !aftaler.isEmpty()){
			List<Aftale> retur = new ArrayList<Aftale>(aftaler.size());
			for (EgenskabHolder aftale : aftaler) {
				if (!aftale.isOphoert(pDato) && !aftale.isAnnulleret()){
					retur.add((Aftale)aftale);
				}
			}
			if (!retur.isEmpty()){
				return ContainerUtil.toArray(retur);
			}
		}
		return null;
	}
	
	/**
	 * Som getAftaler(String pPolicenummer, BigDecimal pDato) excl døde tilbud.
	 * @since 2.3
	 */
	public static Aftale[] getAftalerExclDoedeTilbud(String pPolicenummer, BigDecimal pDato) {
		List<Aftale> result = new ArrayList<Aftale>();
		Aftale[] aftaler = getAftaler(pPolicenummer, pDato);
		for (int i = 0; aftaler != null && i < aftaler.length; i++) {
			if(!aftaler[i].isTilbud() || (aftaler[i].isTilbud() && !aftaler[i].isDoedTilbud())) {
				result.add(aftaler[i]);
			}
		}
		return ContainerUtil.toArray(result);
	}
	/**
	 * Denne metode bør kun anvendes i testcases, da udsøgningen er for upræcis.<br>
	 * Metoden forudsætter at selskabet har policenummer som første felt i label og der er ingen periodiseringstjek<br>
	 * Der returneres primært en aftale uden ophørsdato, hvis ingen den første og bedste ophørte
	 * 
	 * @param pPolnr
	 * @return en Aftale med argumentet først i aftalelabel 
	 */
	public static Aftale getAftaleViaLabel(String pPolnr) {
		OQuery qry = new OQuery(XOAftaleLabelImpl.class);
		qry.add(pPolnr+"%", "aftalelabel", OQuery.LIKE);
		qry.addJoin(AftaleImpl.class.getName() + ".aftaleId", 
				XOAftaleLabelImpl.class.getName() + ".Aftale");
		qry.addOrder("oph", AftaleImpl.class.getName(), OQuery.ASC); // ikke-ophørte først
		qry.setMaxCount(1);
		XOAftaleLabelImpl[] labels = (XOAftaleLabelImpl[])DBServer.getInstance().getVbsf().queryExecute(qry);
		return labels != null && labels.length > 0 ? labels[0].getAftale() : null;
	}

	/**
	 * Returnerer alle aftaler (forsikringer) svarende til et policenummer<br>
	 * TODO Der skal søges i både gammelt og nyt policenr
	 * 
	 * @param pPolicenummer
	 *            Policenummer
	 * @return Aftalerne (forsikringerne) svarende til policenummeret. Er null hvis der ikke kunne findes nogen aftaler
	 */
	public static List<Aftale> getAftaler(String pPolicenummer) {
		if (pPolicenummer == null || pPolicenummer.trim().length() < 1)
			return null;
		Egenskabsgruppe polnrGrp = EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.POLICENR, AftaleegngrpImpl.class, OQuery.EQUAL);
		Egenskab polnrE = polnrGrp.getEgenskab(pPolicenummer);
		if (polnrE == null){
			return null; // så er den heller som gl. policenr
		}
		List<EgenskabHolder> aftaler = polnrE.getEgenskabHoldere(null); // null=relationer uden ophør 
		if (aftaler != null && !aftaler.isEmpty()){
			List<Aftale> retur = new ArrayList<Aftale>(aftaler.size());
			for (EgenskabHolder aftale : aftaler) {
				if (!aftale.isAnnulleret()){
					retur.add((Aftale)aftale);
				}
			}
			if (!retur.isEmpty()){
				return retur;
			}
		}
		return null;
	}

	/**
	 * Returnerer alle aftaler (forsikringer) svarende til et policenummer INcl. evt. annullerede aftaler<br>
	 * TODO Der skal søges i både gammelt og nyt policenr
	 * 
	 * @param pPolicenummer
	 *            Policenummer
	 * @return Aftalerne (forsikringerne) svarende til policenummeret. Er null hvis der ikke kunne findes nogen aftaler
	 */
	public static List<Aftale> getAftalerInclAnnulleret(String pPolicenummer) {
		if (pPolicenummer == null || pPolicenummer.trim().length() < 1)
			return null;
		Egenskabsgruppe polnrGrp = EgenskabsgruppeImpl.getEgenskabsgruppe(null, Aftaleegngrp.POLICENR, AftaleegngrpImpl.class, OQuery.EQUAL);
		Egenskab polnrE = polnrGrp.getEgenskab(pPolicenummer);
		if (polnrE == null){
			return null; // så er den heller som gl. policenr
		}
		List<EgenskabHolder> aftaler = polnrE.getEgenskabHoldere(null); // null=relationer uden ophør 
		if (aftaler != null && !aftaler.isEmpty()){
			List<Aftale> retur = new ArrayList<Aftale>(aftaler.size());
			for (EgenskabHolder aftale : aftaler) {
				retur.add((Aftale)aftale);
			}
			if (!retur.isEmpty()){
				return retur;
			}
		}
		return null;
	}

	@Override
	public BigDecimal getFleraarigUdloeb(BigDecimal pDato){
		Egenskabsgruppe fleraarigUdl = EgenskabsgruppeImpl.getEgenskabsgruppe(null,
				Aftaleegngrp.FLERAARIG_UDLOEBSDATO, AftaleegngrpImpl.class, OQuery.EQUAL);
		if (fleraarigUdl != null){
			Egenskab fleraarigudloeb = getFelt(fleraarigUdl, pDato);
			if (fleraarigudloeb != null){
				GensamSkaermDB g = fleraarigUdl.getEgenskabDatatype();
				if (g instanceof GensamSkaermDBDate){
					g.setDBValue(fleraarigudloeb.getBenaevnelseTrim());// DDMMYYYY
					return ((GensamSkaermDBDate)g).getDBFormat();
				}
			}
		}
		return null;
	}
	 /**
     * 
     * finder alle aftaleegenskaber til aftale
     * @return Egenskab[]
     *
     */
    public Egenskab[] getEgenskaberAlle() {
    	AftaleAfegn[]  afAfeg = this.getAftaleAfegn(null);
		if ((afAfeg == null) || (afAfeg.length == 0)) {
			return null;
		}
		ArrayList<Egenskab> afegLst = new ArrayList<Egenskab>();
		for (int i = 0; (i < afAfeg.length); i++) {
			Egenskab afeg = afAfeg[i].getEgenskab();
			afegLst.add(afeg);
		}
		return ContainerUtil.toArray(afegLst);
	}


	public boolean hasFleraarigTegning(BigDecimal dato) {
		Egenskab tegningsperiodeDirekte = getTegningsperiodeDirekte(dato);
		if(tegningsperiodeDirekte != null) {
			BigDecimal periode = new BigDecimal(tegningsperiodeDirekte.getKortBenaevnelseTrim());
			if(periode.compareTo(BigDecimal.ONE) > 0) {
				return true;
			}
		}
		return false;
	}

	public Egenskab getTegningsperiodeDirekte(BigDecimal dato) {
		Aftaleegngrp[] values = (Aftaleegngrp[])QueryService.stdQuery(AftaleegngrpImpl.class, "kortBenaevnelse", "TEGNPER", QueryService.STARTMED, 0);
		for(int i=0; values != null && i < values.length; i++) {
			Aftaleegngrp grp = values[i];
			EgenskabHolderEgenskab ehe = EgenskabHolderImpl.findEgenskabHolderEgenskab(this, grp, dato);
			if(ehe != null) {
				return ehe.getEgenskab();
			}
		}
		
		values = (Aftaleegngrp[])QueryService.stdQuery(AftaleegngrpImpl.class, "kortBenaevnelse", "FLERÅRIG", QueryService.STARTMED, 0);
		for(int i=0; values != null && i < values.length; i++) {
			Aftaleegngrp grp = values[i];
			EgenskabHolderEgenskab ehe = EgenskabHolderImpl.findEgenskabHolderEgenskab(this, grp, dato);
			if(ehe != null) {
				return ehe.getEgenskab();
			}
		}
		
		values = (Aftaleegngrp[])QueryService.stdQuery(AftaleegngrpImpl.class, "kortBenaevnelse", "FLERÅRTLBO", QueryService.STARTMED, 0);
		for(int i=0; values != null && i < values.length; i++) {
			Aftaleegngrp grp = values[i];
			EgenskabHolderEgenskab ehe = EgenskabHolderImpl.findEgenskabHolderEgenskab(this, grp, dato);
			if(ehe != null) {
				return ehe.getEgenskab();
			}
		}
		
		values = (Aftaleegngrp[])QueryService.stdQuery(AftaleegngrpImpl.class, "kortBenaevnelse", "FLERÅRTEJE", QueryService.STARTMED, 0);
		for(int i=0; values != null && i < values.length; i++) {
			Aftaleegngrp grp = values[i];
			EgenskabHolderEgenskab ehe = EgenskabHolderImpl.findEgenskabHolderEgenskab(this, grp, dato);
			if(ehe != null) {
				return ehe.getEgenskab();
			}
		}
		
		values = (Aftaleegngrp[])QueryService.stdQuery(AftaleegngrpImpl.class, "kortBenaevnelse", "FLERTEGN", QueryService.STARTMED, 0);
		for(int i=0; values != null && i < values.length; i++) {
			Aftaleegngrp grp = values[i];
			EgenskabHolderEgenskab ehe = EgenskabHolderImpl.findEgenskabHolderEgenskab(this, grp, dato);
			if(ehe != null) {
				return ehe.getEgenskab();
			}
		}
		
		return null;
	}		



 

 // genererede accessmetoder til AftaleRev
  public AftaleRev[] getAftaleRev()  {
    return (AftaleRev[]) DBServer.getInstance().getVbsf().get(this, "AftaleRev"); 
  }
  public void addAftaleRev(AftaleRev pAftaleRev) {
	  PersistensService.addToCollection(this, "AftaleRev", pAftaleRev);
  }
  public void removeAftaleRev(AftaleRev oldAftaleRev)  {
	  PersistensService.removeFromCollection(this, "AftaleRev", oldAftaleRev);
  }

 

 // genererede accessmetoder til Brev
  public Brev[] getBrev()  {
    return (Brev[]) DBServer.getInstance().getVbsf().get(this, "Brev"); 
  }
  public void addBrev(Brev pBrev) {
	  PersistensService.addToCollection(this, "Brev", pBrev);
  }
  public void removeBrev(Brev oldBrev)  {
	  PersistensService.removeFromCollection(this, "Brev", oldBrev);
  }

//	@Override
//	public boolean harBrugerbestemtPraemie(BigDecimal pDato) {
//		
//		OQuery qry = new OQuery(ProduktYdtpAngImpl.class);
//		qry.add(this.getId(), "Aftale");
//		qry.addJoin(ProduktYdtpAngImpl.class.getName()+".Ydelsestype", YdelsestypeImpl.class.getName()+".ydelsestypeId");
////		qry.addJoin(ProduktYdtpAngImpl.class.getName()+".Produkt", ProduktImpl.class.getName()+".produktId");
//		qry.add(Ydelsestype.BRUGERBESTEMT_PRAEMIE+"%", YdelsestypeImpl.class.getName()+".benaevnelse", OQuery.LIKE );
//		qry = DBServer.getInstance().getVbsf().getGldQuery(qry, pDato);
//		qry.setMaxCount(1);
//		qry.addOrder(ProduktImpl.class.getName()+".oph"); // virker ikke - bliver pdydtang.oph
////		int count = QueryService.getCount(qry);
//		ProduktYdtpAngImpl[] rels = (ProduktYdtpAngImpl[])DBServer.getInstance().getVbsf().queryExecute(qry);
//		
//		if (rels == null) {
//			// så har aftalen helt sikkert ingen bbp på datoen
//			return false;
//		}
//		if (rels[0].getProdukt().isGld(pDato)) {
//			// så behøver vi ikke at tjekke mere
//			return true;
//		}
//			
//		// Der er altså en bbp, men gælder den nu lige i dag og gør dækningen?
//		
//		Produkt[] produkter = getDaekningerGld(pDato);
//		if (produkter != null) {
//			for (Produkt produkt : produkter) {
//				if (produkt != null) {
//					if (produkt.harBrugerbestemtPraemie(pDato)) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
	@Override
	public boolean harBrugerbestemtPraemie(BigDecimal pDato) {
		ProduktYdtpAng[] produktYdtpAng = this.getProduktYdtpAng(pDato, QueryService.Datefilters.GLD);
		if (produktYdtpAng != null) {
			for (ProduktYdtpAng y : produktYdtpAng) {
	  			if (y.getYdelsestype().isBrugerbestemtPraemie()){
	  				Produkt daekning = y.getProdukt();
	  				if (daekning.isGld(pDato))
	  					return true;
	  			}
			}
		}
	
//		Produkt[] produkter = getDaekningerGld(pDato);
//		if (produkter != null) {
//			for (Produkt produkt : produkter) {
//				if (produkt != null) {
//					if (produkt.harBrugerbestemtPraemie(pDato)) {
//						return true;
//					}
//				}
//			}
//		}
		return false;
	}
	
	/**
	 * Undersøger om der findes OprettelsesGebyrOmkostningstypeRegler tilknyttet forsikringen pr. den medsendte pDato.<br>
	 * OprettelsesGebyrOmkostningstypeRegler (OMTP, AFTPOMTP) bruges sammen med specielle GebyrtypeRegler til at afgøre om der skal dannes oprettelsesgebyr ved oprettelse af forsikringen eller ved vognskift.<br>
	 *
	 * @param pDato
	 * @return <code>true</code> hvis OprettelsesGebyrOmkostningstypeRegler findes. Ellers <code>false</code>.
	 */
	public boolean harOprettelsesGebyrOmkostningstypeRegler(BigDecimal pDato) {
		//Findes der overhovedet omkostningstypen "OPRETGEBYR" ?
		Omkostningstype omtp = (Omkostningstype) DBServer.getInstance().getVbsf().lookupRegelType(OmkostningstypeImpl.class, Omkostningstype.OPRETTELSESGEBYR);
		if (omtp == null){//ingen grund til at gå videre...
			return false; 
		}
		
		//Vi tjekker lige AFTPOMTP
		AftaletypeOmkostningstype[] aftaletypeOmkostningstype = getAftaleTypen().getAftaletypeOmkostningstype();
		if (aftaletypeOmkostningstype != null){
			for (AftaletypeOmkostningstype aftpomtp: aftaletypeOmkostningstype){
				if (aftpomtp != null && aftpomtp.isGld(pDato) && aftpomtp.getOmkostningstype().isOprettelsesgebyr()){
					return true;
				}
				
			}
		}
		
		return false;
	}

	/**
	 * Undersøger om det er den nyeste provisionsaftalehaendelse på hændelsesdatoen.
	 * @since 29/09/2009 Gensafe Pro 2.2
	 */
	public boolean isAftalehaendelseNyeste(Aftalehaendelse pAftalehaendelse) {
		if(pAftalehaendelse != null) {
			Aftalehaendelse aftalehaendelseNyeste = getAftalehaendelseNyeste(pAftalehaendelse.getHaendelsesdato(), true);
			if(aftalehaendelseNyeste != null) {
				return aftalehaendelseNyeste.equals(pAftalehaendelse);
			}
			
		}
		return false;
	}
	
	public boolean harStatusKlarOphoer() {
		if (getOph().compareTo(BigDecimal.ZERO) > 0) {
			AftaleStatustype[] afsttps = getAftaleStatustyperMedGld(Datobehandling.datoPlusMinusAntal(getOph(), 1));
			for (int i = 0; afsttps != null && i < afsttps.length; i++) {
				if (afsttps[i].getStatustype().getKortBenaevnelse().trim().equals(Statustype.KLAROPHOER) && 
						!afsttps[i].isOphUdfyldt()) {
					return true;
				}
			}
		}
		return false;
	} 

 // genererede accessmetoder til AftalePBSDebitorGruppeNr
  public AftalePBSDebitorGruppeNr[] getAftalePBSDebitorGruppeNr()  {
    return (AftalePBSDebitorGruppeNr[]) DBServer.getInstance().getVbsf().get(this, "AftalePBSDebitorGruppeNr"); 
  }
  
  /**
   * @param pDato
   * @return aftalens gældende relation pr. pDato til PBSDebitorGruppeNr.
   */
  public AftalePBSDebitorGruppeNr getAftalePBSDebitorGruppeNr(BigDecimal pDato)  {
	  BigDecimal dato = pDato;
	  if(Datobehandling.isFremtidig(this.getGld(), dato)){
		  dato = this.getGld();		  
	  }
	  if(this.isOphoert(dato)){
		  if(this.getGld().compareTo(this.getOph()) > 0) {
			  dato = this.getGld();
		  } else {
			  dato = this.getOph();
		  }
	  }
	  
	  
	  AftalePBSDebitorGruppeNr[] afPBSDebGrpNrTabel =  (AftalePBSDebitorGruppeNr[]) DBServer.getInstance().getVbsf().get(this, "AftalePBSDebitorGruppeNr", dato);
	  if ((afPBSDebGrpNrTabel != null) && (afPBSDebGrpNrTabel.length > 0)) {
		  return afPBSDebGrpNrTabel[0];
	  }
	  return null;
  }
  
  /**
   * @param pDato
   * @return aftalens fremtidige relationer (ifht. pDato) til PBSDebitorGruppeNr
   */
  public AftalePBSDebitorGruppeNr[] getAfPBSDebitorGruppeNrFremtidige(BigDecimal pDato) {
	  AftalePBSDebitorGruppeNr[] afPBSDebGrpNrTabel = getAftalePBSDebitorGruppeNr();
		return (AftalePBSDebitorGruppeNr[]) Datobehandling.findFremtidige(afPBSDebGrpNrTabel, pDato);
	}
  public AftalePBSDebitorGruppeNr[] getAfPBSDebitorGruppeNrGaeldendeOgFremtidige(BigDecimal pDato) {
	  AftalePBSDebitorGruppeNr[] afPBSDebGrpNrTabel = getAftalePBSDebitorGruppeNr();
		return (AftalePBSDebitorGruppeNr[]) Datobehandling.findGaeldendeOgFremtidige(afPBSDebGrpNrTabel, pDato);
	}
  
  public void addAftalePBSDebitorGruppeNr(AftalePBSDebitorGruppeNr pAftalePBSDebitorGruppeNr) {
	  PersistensService.addToCollection(this, "AftalePBSDebitorGruppeNr", pAftalePBSDebitorGruppeNr);
  }
  public void removeAftalePBSDebitorGruppeNr(AftalePBSDebitorGruppeNr oldAftalePBSDebitorGruppeNr)  {
	  PersistensService.removeFromCollection(this, "AftalePBSDebitorGruppeNr", oldAftalePBSDebitorGruppeNr);
  }

 

 // genererede accessmetoder til AftaleArtplog
  public AftaleArtplog[] getAftaleArtplog()  {
    return (AftaleArtplog[]) DBServer.getInstance().getVbsf().get(this, "AftaleArtplog"); 
  }
  public void addAftaleArtplog(AftaleArtplog pAftaleArtplog) {
	  PersistensService.addToCollection(this, "AftaleArtplog", pAftaleArtplog);
  }
  public void removeAftaleArtplog(AftaleArtplog oldAftaleArtplog)  {
	  PersistensService.removeFromCollection(this, "AftaleArtplog", oldAftaleArtplog);
  }

 

 // genererede accessmetoder til PBSAutoBetaling
  public PBSAutoBetaling[] getPBSAutoBetaling()  {
    return (PBSAutoBetaling[]) DBServer.getInstance().getVbsf().get(this, "PBSAutoBetaling"); 
  }
  public void addPBSAutoBetaling(PBSAutoBetaling pPBSAutoBetaling) {
	  PersistensService.addToCollection(this, "PBSAutoBetaling", pPBSAutoBetaling);
  }
  public void removePBSAutoBetaling(PBSAutoBetaling oldPBSAutoBetaling)  {
	  PersistensService.removeFromCollection(this, "PBSAutoBetaling", oldPBSAutoBetaling);
  }

  public MarkeringAdresseFlytning[] getMarkeringAdresseFlytning()  {
	return (MarkeringAdresseFlytning[]) DBServer.getInstance().getVbsf().get(this, "MarkeringAdresseFlytning");
  }
  public void addMarkeringAdresseFlytning(MarkeringAdresseFlytning pMarkeringAdresseFlytning) {
	PersistensService.addToCollection(this, "MarkeringAdresseFlytning", pMarkeringAdresseFlytning);
  }
  public void removeMarkeringAdresseFlytning(MarkeringAdresseFlytning oldMarkeringAdresseFlytning)  {
	PersistensService.removeFromCollection(this, "MarkeringAdresseFlytning", oldMarkeringAdresseFlytning);
  }

 // genererede accessmetoder til PBSINDAftaleoplysning
  public PBSINDAftaleoplysning[] getPBSINDAftaleoplysning()  {
    return (PBSINDAftaleoplysning[]) DBServer.getInstance().getVbsf().get(this, "PBSINDAftaleoplysning"); 
  }
  public void addPBSINDAftaleoplysning(PBSINDAftaleoplysning pPBSINDAftaleoplysning) {
	  PersistensService.addToCollection(this, "PBSINDAftaleoplysning", pPBSINDAftaleoplysning);
  }
  public void removePBSINDAftaleoplysning(PBSINDAftaleoplysning oldPBSINDAftaleoplysning)  {
	  PersistensService.removeFromCollection(this, "PBSINDAftaleoplysning", oldPBSINDAftaleoplysning);
  }

 

 // genererede accessmetoder til PBSTilhoersskift
  public PBSTilhoersskift[] getPBSTilhoersskift()  {
    return (PBSTilhoersskift[]) DBServer.getInstance().getVbsf().get(this, "PBSTilhoersskift"); 
  }
  public void addPBSTilhoersskift(PBSTilhoersskift pPBSTilhoersskift) {
	  PersistensService.addToCollection(this, "PBSTilhoersskift", pPBSTilhoersskift);
  }
  public void removePBSTilhoersskift(PBSTilhoersskift oldPBSTilhoersskift)  {
	  PersistensService.removeFromCollection(this, "PBSTilhoersskift", oldPBSTilhoersskift);
  }
  
	public PBSTilmelding[] getPBSTilmeldinger() {
		return (PBSTilmelding[]) DBServer.getInstance().getVbsf().get(this, "PBSTilmelding");
	}

	public PBSTilmelding getPBSTilmelding() {
		PBSTilmelding[] pbsTilmeldinger = (PBSTilmelding[]) DBServer.getInstance().getVbsf().get(this, "PBSTilmelding");
		if (pbsTilmeldinger != null && pbsTilmeldinger.length > 0) {
			if (pbsTilmeldinger.length > 1){
				Arrays.sort(pbsTilmeldinger, new ModelObjektIdComparator(true));
//				throw new DatafejlException("Flere PBSTilmeldinger til samme aftale ikke muligt - AftaleID = " + this.getId());
				/**
				 * Vi har valgt at der kun er en relation med tilmeldingsoplysninger. 
				 * Skal der sendes en ny ændres status og de andre oplysninger på den eksisterende pbs-tilmelding.
				 * Historik  findes i PBSUD Aftaleoplysning. 
				 * LF
				 * marts 2012: da der ønskes bedre sporing af fortrudte til- og afmeldinger, tillader vi flere, og vælger den nyeste af evt.flere
				 */
			}
			return pbsTilmeldinger[0];
		}
		return null;
	}

  /** 
   * Returnerer true hvis tilbuddet er blevet udskrevet
   * OBS 11A6
   */
  public boolean isTilbudPrinted() {
	  AftaleUdskriftLaas[] aftaleUdskriftLaase = getAftaleUdskriftLaas();

	  if (aftaleUdskriftLaase != null && aftaleUdskriftLaase.length > 0) {
		  for (AftaleUdskriftLaas aftaleUdskriftLaas : aftaleUdskriftLaase) {
			  if (aftaleUdskriftLaas.getStatus().trim().equalsIgnoreCase(AftaleUdskriftLaas.UDSKREVET)) {
				  return true;
			  }
		  }
	  }

	  return false;
  }

 

  /** 
   * Returnerer de aftalelåse der er tilknyttet. Bruges kun til tilbud
   **/
  public AftaleUdskriftLaas[] getAftaleUdskriftLaas()  {
    return (AftaleUdskriftLaas[]) DBServer.getInstance().getVbsf().get(this, "AftaleUdskriftLaas"); 
  }
  public void addAftaleUdskriftLaas(AftaleUdskriftLaas pAftaleUdskriftLaas) {
	  PersistensService.addToCollection(this, "AftaleUdskriftLaas", pAftaleUdskriftLaas);
  }
  public void removeAftaleUdskriftLaas(AftaleUdskriftLaas oldAftaleUdskriftLaas)  {
	  PersistensService.removeFromCollection(this, "AftaleUdskriftLaas", oldAftaleUdskriftLaas);
  }

	@Override
	public PBSDebitorGruppeNr getPBSDebitorGruppeNrSenesteGld() {
		OQuery qry = QueryService.queryCreate(AftalePBSDebitorGruppeNrImpl.class);
		qry.add(this.getId(), "Aftale", QueryService.EQUAL);
		qry.addOrder("gld", OQuery.DESC);
		qry.setMaxCount(1);
		AftalePBSDebitorGruppeNr[] afpbsdeb = (AftalePBSDebitorGruppeNr[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (afpbsdeb != null) {
			if (afpbsdeb.length > 0 && afpbsdeb[0] != null) {
				return afpbsdeb[0].getPBSDebitorGruppeNr();
			}
		}
		return null;
	}
	
	/**
	 * 
	 * opbygger  {@link BonusForloebFiktiv} udfra et fiktivt skadeforløb og et aktuelt bonusforløb 
	 * og returnerer disse i et array
	 * @param pDato = dato for udvælgelse af genstande, bonustype m.m.
	 * @param pFiktivskade = om der skal lægges 1 til belastende skader eller ej. (om kald fra forsikringsfanen eller skadefanen)
	 *
	 */
	public BonusForloebFiktiv[] getBonusForloebFiktiv(BigDecimal pDato, boolean pFiktivSkade) {
		
		if(pDato==null) {
			pDato = Datobehandling.getDagsdatoBigD();
		}
		BigDecimal ultimativStopklods = Datobehandling.datoPlusMinusAntalAar(pDato, 20);
		Bonustype bffBonustype = null;
		int bffAntalBonusbelastendeSkader = 0;
		ArrayList<BonusForloebFiktiv> bff = new ArrayList<BonusForloebFiktiv>();
		List<Genstand> genstande = this.getGenstande(pDato);
		BigDecimal datoPlusEnTilAtHenteAlleSkader = null;
		datoPlusEnTilAtHenteAlleSkader = pDato;
		if(genstande != null) {
			for (Genstand genstand : genstande) {
				if(genstand.getGenstandsTypen().harBonustype() && !this.isOphoert(pDato)) {
					//udsøg for fiktiv skadeforløb
					boolean sidsteBonustrin = false;
					boolean foersteGennemloeb = true;
					Bonustype aktuelBonustype = null;
					Bonustype bonustypeNaeste = null;
					BigDecimal bonusReguleringsDato = null;
					int antalSkadefriMaaneder = 0;
					int antalMaanederSidenSidsteRegulering = 0;
					int antalBonusbelastendeSkader = 0;
					do {
						if(aktuelBonustype!=null && bonustypeNaeste != null) {
							if(!aktuelBonustype.equals(bonustypeNaeste)) {
								antalMaanederSidenSidsteRegulering = 0;
							}
						}
						antalSkadefriMaaneder = antalSkadefriMaaneder + 12;
						antalMaanederSidenSidsteRegulering = antalMaanederSidenSidsteRegulering + 12;
						antalBonusbelastendeSkader = 0;
						aktuelBonustype = bonustypeNaeste;
						BonusreguleringsHelper bonusreguleringsHelper = new BonusreguleringsHelper(this, datoPlusEnTilAtHenteAlleSkader);
						if(foersteGennemloeb) {
							bonusReguleringsDato = this.getFoerstKommendeBonusreguleringsdato(pDato);
							if(bonusReguleringsDato==null) {
								bonusReguleringsDato = this.getIkkeUdfoertFornyelsesopgave();
							}
							aktuelBonustype = genstand.getBonusType(pDato);
							antalBonusbelastendeSkader = bonusreguleringsHelper.getAntalBonusbelastendeSkader();
							if(pFiktivSkade) {
								antalBonusbelastendeSkader = antalBonusbelastendeSkader + 1;
							}
							bffBonustype =  genstand.getBonusType(pDato);
							bffAntalBonusbelastendeSkader = antalBonusbelastendeSkader;
							antalMaanederSidenSidsteRegulering = bonusreguleringsHelper.getAntalMaanederIBonustype(aktuelBonustype);
							antalSkadefriMaaneder = bonusreguleringsHelper.getAntalSkadeFriMaaneder();
						}

						BonustypeInfo bonustypeInfo = bonusreguleringsHelper.findNyBonustypeInfo(aktuelBonustype, antalBonusbelastendeSkader, antalSkadefriMaaneder, antalMaanederSidenSidsteRegulering);
						if(bonustypeInfo == null) {
							bonustypeNaeste = aktuelBonustype;
						}
						else {
							bonustypeNaeste = bonustypeInfo.getBonustype();
						}
						sidsteBonustrin = bonustypeNaeste.isSidsteBonustrin(pDato);
						BigDecimal bonustypePraemie = bonusreguleringsHelper.getPraemiePaaFiktivBonusType(this, bonusReguleringsDato, bonustypeNaeste);
						BonusForloebFiktiv bonusForloebFiktiv = new BonusForloebFiktiv();
						bonusForloebFiktiv.setAar(bonusReguleringsDato);
						bonusForloebFiktiv.setSkadeForloeb(bonustypeNaeste);
						bonusForloebFiktiv.setSkadeForloebPraemie(bonustypePraemie);
						bonusForloebFiktiv.setAntalBelastendeSkader(bffAntalBonusbelastendeSkader);
						bonusForloebFiktiv.setAktuelBonustype(bffBonustype);
						bff.add(bonusForloebFiktiv);
						bonusReguleringsDato = Datobehandling.datoPlusMinusAntalAar(bonusReguleringsDato, 1);
						pDato = bonusReguleringsDato;
						foersteGennemloeb = false;
					} while (!sidsteBonustrin && bonusReguleringsDato.compareTo(ultimativStopklods) <= 0);
					
					//udsøg for aktuel skadeforløb
					foersteGennemloeb = true;
					sidsteBonustrin = false;
					BigDecimal bonustypePraemie = null;
					antalSkadefriMaaneder = 0;
					antalMaanederSidenSidsteRegulering = 0;
					antalBonusbelastendeSkader = 0;
					for (int i = 0; bff != null && i < bff.size(); i++) {
						if(aktuelBonustype!=null) {
							if(!aktuelBonustype.equals(bonustypeNaeste)) {
								antalMaanederSidenSidsteRegulering = 0;
							}
						}
						antalBonusbelastendeSkader = 0;
						antalSkadefriMaaneder = antalSkadefriMaaneder + 12;
						antalMaanederSidenSidsteRegulering = antalMaanederSidenSidsteRegulering + 12;
						aktuelBonustype = bonustypeNaeste;
						BonusreguleringsHelper bonusreguleringsHelper = new BonusreguleringsHelper(this, bff.get(i).getAar());
						bonusReguleringsDato = bff.get(i).getAar();
						if(foersteGennemloeb) {
							antalBonusbelastendeSkader = bonusreguleringsHelper.getAntalBonusbelastendeSkader();
							if(pFiktivSkade) {
								aktuelBonustype = bff.get(0).getAktuelBonustype();
							}
							if(!pFiktivSkade) {
								if(antalBonusbelastendeSkader > 0) {
									antalBonusbelastendeSkader = antalBonusbelastendeSkader - 1;
								}
							}
							// Det kan være foretaget vogn- eller produktskifte, så vi kan
							// ikke være sikre på, at aktuelle genstand er gælende pr. skadePeriodeFra
							// Hvis dette er tilfældet anvendes genstand.getGld()
							// Ass. 16219.
							BigDecimal skadePeriodeFra = bonusreguleringsHelper.getSkadePeriodeFra();
							if(genstand.isGld(skadePeriodeFra)){
								aktuelBonustype = genstand.getBonusType(skadePeriodeFra);
							} else {
								aktuelBonustype = genstand.getBonusType(genstand.getGld());
							}
							antalMaanederSidenSidsteRegulering = bonusreguleringsHelper.getAntalMaanederIBonustype(aktuelBonustype);
							if(pFiktivSkade || !pFiktivSkade && antalBonusbelastendeSkader > 0) {
								antalSkadefriMaaneder = bonusreguleringsHelper.getAntalSkadeFriMaaneder();
							}
						}
						BonustypeInfo bonustypeInfo = new BonustypeInfo();
						bonustypeInfo = bonusreguleringsHelper.findNyBonustypeInfo(aktuelBonustype, antalBonusbelastendeSkader, antalSkadefriMaaneder, antalMaanederSidenSidsteRegulering);
						if(bonustypeInfo == null) {
							bonustypeNaeste = aktuelBonustype;
						}
						else {
							bonustypeNaeste = bonustypeInfo.getBonustype();
						}
//						if(!sidsteBonustrin) { nu kalder vi i alle tilfælde da datoen er afgørende eks. aldersrabat
							bonustypePraemie = bonusreguleringsHelper.getPraemiePaaFiktivBonusType(this, bonusReguleringsDato, bonustypeNaeste);
							sidsteBonustrin = bonustypeNaeste.isSidsteBonustrin(bonusReguleringsDato);
//						}
						bff.get(i).setAktuelForloeb(bonustypeNaeste);
						bff.get(i).setAktuelForloebPraemie(bonustypePraemie);
						foersteGennemloeb = false;
					}
					break;
				}
			}
		}
		return ContainerUtil.toArray(bff);
	}

	// genererede accessmetoder til DMRMeddelelseDb
	public DMRMeddelelseDb[] getDMRMeddelelseDb() {
		return (DMRMeddelelseDb[]) DBServer.getInstance().getVbsf().get(this, "DMRMeddelelseDb");
	}

	public void addDMRMeddelelseDb(DMRMeddelelseDb pDMRMeddelelseDb) {
		PersistensService.addToCollection(this, "DMRMeddelelseDb", pDMRMeddelelseDb);
	}

	public void removeDMRMeddelelseDb(DMRMeddelelseDb oldDMRMeddelelseDb) {
		PersistensService.removeFromCollection(this, "DMRMeddelelseDb", oldDMRMeddelelseDb);
	}

	@Override
    public AftalekompYdtpAngivelseIF[] getYdelsesAngivelserInclAnnullerede() {
		return (AftalekompYdtpAngivelseIF[]) super.getHistorikInclAnnullerede(getAftaleYdtpAngGld(null, null), AftaleYdtpAngAnnulleredeImpl.class, "Aftale");
    }

	@Override
	public IndividAftale[] getIndividAftaleInclAnnullerede() {
		return (IndividAftale[]) super.getHistorikInclAnnullerede(getIndividAftale(), IndividAftaleAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public BttpAftale[] getKlausultypeAftaleInclAnnullerede() {
		return (BttpAftale[]) super.getHistorikInclAnnullerede(getBttpAftale(), BttpAftaleAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public AftaleRbtp[] getAftaleRbtpInclAnnullerede() {
		return (AftaleRbtp[]) super.getHistorikInclAnnullerede(getAftaleRabatTyper(), AftaleRbtpAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public AftaleAdresse[] getAftaleAdresseInclAnnullerede() {
		return (AftaleAdresse[]) super.getHistorikInclAnnullerede(getAftaleAdresseGldOgFremtidige(null), AftaleAdresseAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public AftaleFrekvens[] getAftaleFrekvensInclAnnullerede() {
		return (AftaleFrekvens[]) super.getHistorikInclAnnullerede(getAftaleFrekvens(), AftaleFrekvensAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public AftaleArtp[] getAftaleArtpInclAnnullerede() {
		return (AftaleArtp[]) super.getHistorikInclAnnullerede(getAftaleArtp(), AftaleArtpAnnulleredeImpl.class, "Aftale");
	}

	@Override
	public AftaleMinPrae[] getAftaleMinPraeInclAnnullerede() {
		return (AftaleMinPrae[]) super.getHistorikInclAnnullerede(getAftaleMinPrae(), AftaleMinPraeAnnulleredeImpl.class, "Aftale");
	}

	@Override
    public AftaleFftpMd[] getAftaleFftpMdInclAnnullerede() {
		return (AftaleFftpMd[]) super.getHistorikInclAnnullerede(getAftaleFftpMd(), AftaleFftpMdAnnulleredeImpl.class, "Aftale");
    }

	@Override
    public EgenskabHolderEgenskab[] getEgenskabHolderEgenskabInclAnnullerede() {
		return (EgenskabHolderEgenskab[]) super.getHistorikInclAnnullerede(getAftaleAfegn(null), AftaleAfegnAnnulleredeImpl.class, "EgenskabHolder");
    }

	/**
     * @return arrray med alle gældende reassuranceaftaler sorteret på getTegnesAfIndividLabel
     */
    public static Aftale[] getReassuranceAftaleOrderedByIndividLabel() {
	    String reasAfTpKortBen = Aftaletype.REASSURANCE_AFTALETYPE;
	    String clsAftale = AftaleImpl.class.getName();
	    
	    String clsAftaletype = AftaletypeImpl.class.getName();
	    BigDecimal ophoer = BigDecimal.ZERO;
	    
	    OQuery qry = QueryService.queryCreate(AftaleImpl.class);
	    qry.add(reasAfTpKortBen, clsAftaletype + ".kortBenaevnelse", OQuery.EQUAL);
	    qry.add(ophoer, clsAftale + ".oph", OQuery.EQUAL);
	    qry.addJoin(clsAftale + ".typeId", clsAftaletype + ".aftaletypeId");

	    Aftale[] reasAftalerx = (Aftale[]) DBServer.getInstance().getVbsf().queryExecute(qry); 
	    
	    // Hvis der fandtes reasAftaler, sorter ud fra individ label
	    if(reasAftalerx != null) {
	    	Arrays.sort(reasAftalerx, new Comparator<Aftale>() {
	    		public int compare(Aftale a, Aftale b) {
	    			return a.getTegnesAfIndividLabel().compareTo(b.getTegnesAfIndividLabel());
	    		}
	    	});
	    }
	    else {
	    	// Der fandtes ikke reasAftaler .... uhm.... så kan man ikke oprette nye kontrakter.
	    	// Skal det overhovedet håndteres? Kan man komme hertil uden at ha' reasAftaler overhovedet?
	    }
	    return reasAftalerx;
    }
    
    public boolean isKlarTilAnonymisering(BigDecimal pAnonymiseringsdato) {
    	boolean tilbud = this.isTilbud();
    	if (!this.isForsikringsAftale() || (tilbud && this.isTilbudAaben()) || (!(tilbud) && !this.isOphUdfyldt())) {
    		return false;
    	}
    	
    	boolean individDatoFindes = this.getTegnesAfIndivid().harAnonymiseringsDato();
    	BigDecimal individDato = BigDecimal.ZERO;
    	if (individDatoFindes) {
    		individDato = this.getTegnesAfIndivid().getAnonymiseringsDato();
    		// Hvis IndividDato ikke er nået eller overskredet er det helt ligegyldigt med alt andet
    		if (individDato.compareTo(pAnonymiseringsdato) > 0) {
    			return false;
    		}
    	}

		// Hvis elektronisk overført til Nærsikring anonymiseres aldrig
		if (this.isElektroniskOverfoert()) {
			return false;
		}

    	
    	BigDecimal compareOphDato = BigDecimal.ZERO;
    	BigDecimal ophDato = (tilbud ? this.getOprDato() : this.getOph());
    	if (tilbud || this.isAnnulleret()) {
    		// Annullerede aftaler og Tilbud har kort frist og har ingen skadesager der skal tages hensyn til
    		// IndividDato overruler normal-frist - ophøret skal blot være passeret
    		if (individDatoFindes) {
    			compareOphDato = ophDato;
    		}
    		else {
    			int maaneder = 0;
	            if (tilbud) {
		            maaneder = AnonymiseringsFristerImpl.getAnonymiseringsFrist(
		            			AnonymiseringsFristType.TILBUD.getKortBenaevnelse()).intValue();
	            } else {
		            maaneder = AnonymiseringsFristerImpl.getAnonymiseringsFrist(
		                    	AnonymiseringsFristType.ANNULLERET_AFTALE.getKortBenaevnelse()).intValue();
	            }
	            compareOphDato = Datobehandling.datoPlusMinusAntalMd(ophDato, maaneder);
            }
			if (compareOphDato.compareTo(pAnonymiseringsdato) < 0) {
				return true;
			}
			return false;
		}
    	
    	// (Optimering) Ingen grund til at se på skadesager hvis aftalen i sig selv ikke er gammel nok.
    	// IndividDato overruler normal-frist - ophøret skal blot være passeret
    	if (individDatoFindes) {
    		compareOphDato = ophDato;
    	}
    	else {
    		compareOphDato = Datobehandling.datoPlusMinusAntalAar(
    						ophDato, this.getAftaleTypen().getAnonymiseringsfristAar().intValue());
        }
    	if (!(compareOphDato.compareTo(pAnonymiseringsdato) < 0)) {
        		return false;
        }
    	
    	Skadesag[] skSagTab = this.getSkadesager(Aftale.ANONYMISERINGSPERIODE_SKADESAGER);
    	if (skSagTab != null && skSagTab.length > 0) {
    		for (Skadesag skSag : skSagTab) {
    			// Ignorer annullerede
    			if (skSag.isAnnulleret()) {
    				continue;
    			}
    			if (!skSag.isOphUdfyldt() || !skSag.isAfsluttet()) {
    				return false;
    			}
    			// Hvis Aftaletypen er "aldrig hvis skade" stopper vi her
    			if (this.getAftaleTypen().isEjAnonymHvisSkade()) {
    				return false;
    			}
    			// Hvis senere ophør end aftalen bruges skadesagens ophør
    			if (skSag.getOph().compareTo(ophDato) > 0) {
    				ophDato = skSag.getOph();
    			}
    		}
    	}
    	
    	// Er anonymiseringsfristen overskredet -- for aftale og skadesager under et ?
    	if (individDatoFindes) {
    		compareOphDato = ophDato;
    	}
    	else {
    		compareOphDato = Datobehandling.datoPlusMinusAntalAar(
    						ophDato, this.getAftaleTypen().getAnonymiseringsfristAar().intValue());
    	}
    	if (compareOphDato.compareTo(pAnonymiseringsdato) < 0) {
        	return true;
        }
    	
    	return false;
    }

    @Override
	public boolean udfoerTarifering(BigDecimal pTariferingdato) {
		log_.info(GensamUtil.getMemLabel() + "Afvikler statuskørsel Aftale: " + this + " på dato " + pTariferingdato + " "
				+ getAftaletypeKortBenaevnelse());
		Produkt pd = getDaekningTilfaeldig(pTariferingdato);
		if (pd == null) {
			Hovedprodukt[] hovedprodukt = getHovedprodukt();
			pd = DBServer.getInstance().getVbsf().lookup(ProduktImpl.class, hovedprodukt[0].getId());
		}
		if (pd != null) {
			pd.udlaegPDVDLRevidering(pTariferingdato);
			bestilStatusKoersel(false, false, pTariferingdato);
			return true;
		} else {
			log_.error("Ingen aktive dækninger til aftale: " + getId() + " på dato " + pTariferingdato);
		}
		return false;
	}
	public boolean udlaegRevidering(BigDecimal pTariferingdato) {
		Produkt pd = getDaekningTilfaeldig(pTariferingdato);
		if (pd == null) {
			Hovedprodukt[] hovedprodukt = getHovedprodukt();
			pd = DBServer.getInstance().getVbsf().lookup(ProduktImpl.class, hovedprodukt[0].getId());
		}
		if (pd != null) {
			pd.udlaegPDVDLRevidering(pTariferingdato);
			return true;
		} else {
			log_.info("Ingen aktive dækninger til aftale: " + getId() + " på dato " + pTariferingdato);
		}
		return false;
	}

	/**
	 * 
	 * @param pDato
	 * @return en tilfældig gældende dækning på this, null hvis ingen
	 */
	private Produkt getDaekningTilfaeldig(BigDecimal pDato) {
		Produkt[] pdTab = getDaekningerGld(pDato);
		if ((pdTab != null) && (pdTab.length > 0)) {
			return pdTab[0];
		}
		return null;
	}

	public boolean isRykkerKoerselIgang() {
    	return isRykkerKoerselIgang_;
    }

	public void setIsRykkerKoerselIgang_(boolean pIsRykkerKoerselIgang) {
		this.isRykkerKoerselIgang_ = pIsRykkerKoerselIgang;
	}

	@Override
	public WakeupType getWakeupType() {
		return WakeupType.WAKEUPTYPE_AFTALE;
	}

	@Override
	public Wakeup[] getWakeups() {
		return WakeupImpl.getWakeups(this);
	}
	
	@Override
	public Wakeup[] getWakeups(BigDecimal pOphDato) {
		return WakeupImpl.getWakeups(this, pOphDato);
	}	
	
	public boolean isAnonymiseret() {
		return DBServer.getInstance().getVbsf().lookup(AnonymiseretAftaleImpl.class, this.aftaleId) != null;
	}

	@Override
    public Individ getIndivid() {
	    return getTegnesAfIndivid();
    }
	
	public boolean isMedRistornoregler() {
		if (!(isMedRistornoreglerChecket)) {
			isMedRistornoreglerChecket = true;
			isMedRistornoregler = this.getAftaleTypen().isMedRistornoregler(this.getGld());
		}
		return isMedRistornoregler;
	}

	public PBSAfmelding[] getPBSAfmeldinger() {
		try {
			return (PBSAfmelding[]) DBServer.getInstance().getVbsf().get(this, "PBSAfmelding");
		} catch (Exception e) {
			log_.error("Ups har ikke lige fået distribueret PBSAFMD endnu");
		}
		return null;
	}
	
	public PBSAfmelding getPBSAfmelding() {
		try {
			PBSAfmelding[] afmeldinger =  (PBSAfmelding[]) DBServer.getInstance().getVbsf().get(this, "PBSAfmelding");
			if (afmeldinger != null && afmeldinger.length > 0){
				Arrays.sort(afmeldinger, new ModelObjektIdComparator(true));
				return afmeldinger[0];
			}
		} catch (Exception e) {
			log_.error("Ups har ikke lige fået distribueret PBSAFMD endnu");
		}
		return null;
	}


	public void opretManglendeAftaleArtp(BigDecimal pDato) {
		if (getAftaleArtp(pDato) != null) {
			return;
		}
		
		BigDecimal stoersteOphoerFoerDato = BigDecimal.ZERO;
		BigDecimal mindsteGaeldendeEfterDato = GaiaConst.ALL9;
		AftaleArtp[] aftaleArtpTab = this.getAftaleArtp();
		if (aftaleArtpTab != null) {
			for (AftaleArtp aftaleArtp : aftaleArtpTab) {
				if (aftaleArtp.isOphUdfyldt() && aftaleArtp.getOph().compareTo(pDato) < 0 && 
					aftaleArtp.getOph().compareTo(stoersteOphoerFoerDato) > 0) {
					stoersteOphoerFoerDato = aftaleArtp.getOph();
				}
				if (aftaleArtp.getGld().compareTo(pDato) > 0 && 
					aftaleArtp.getGld().compareTo(mindsteGaeldendeEfterDato) < 0) {
					mindsteGaeldendeEfterDato = aftaleArtp.getGld();
				}
			}
		}
		BigDecimal nyGaeldende = this.getGld();
		if (!stoersteOphoerFoerDato.equals(BigDecimal.ZERO)) {
			nyGaeldende = Datobehandling.datoPlusMinusAntal(stoersteOphoerFoerDato, 1);
		}
		BigDecimal nyOphoer = BigDecimal.ZERO;
		if (!mindsteGaeldendeEfterDato.equals(GaiaConst.ALL9)) {
			nyOphoer = Datobehandling.datoPlusMinusAntal(mindsteGaeldendeEfterDato, -1);
		}
		AftaleArtp aftaleArtpTilOpret = PersistensService.opret(AftaleArtpImpl.class);
		aftaleArtpTilOpret.setAftale(this);
		aftaleArtpTilOpret.setGld(nyGaeldende);
		aftaleArtpTilOpret.setOph(nyOphoer);
		aftaleArtpTilOpret.setAfregningstype(AfregningstypeImpl.getGIROAfregningstype());
		aftaleArtpTilOpret.setIndbetaling(GaiaConst.JA);
		AftaleBetalingsfrist aftaleBetalingsfristTilOpret = PersistensService.opret(AftaleBetalingsfristImpl.class);
		aftaleBetalingsfristTilOpret.setAftale(this);
		aftaleBetalingsfristTilOpret.setGldArtp(nyGaeldende);
		aftaleBetalingsfristTilOpret.setGld(nyGaeldende);
		aftaleBetalingsfristTilOpret.setAfregningstype(AfregningstypeImpl.getGIROAfregningstype());
		aftaleBetalingsfristTilOpret.setIndbetaling(GaiaConst.JA);
		boolean harSelvStartetTransaction = PersistensService.transactionBegin();
		PersistensService.gem(aftaleArtpTilOpret);
		PersistensService.gem(aftaleBetalingsfristTilOpret);
		if (harSelvStartetTransaction){
			PersistensService.transactionCommit();
		}
		DBServer.getVbsfInst().discardAll(AftaleArtpImpl.class);
	}

    public List<KlausulholderKlausultype> getKlausulRelationerGldAlleNiveauer(BigDecimal pDato) {
	    List<KlausulholderKlausultype> retur = new ArrayList<>();

        KlausulholderKlausultype[] rels = getBttpAftale();
        if (rels != null){
            for (KlausulholderKlausultype rel : rels){
                if (rel.isGld(pDato))
                    retur.add(rel);
            }
        }

        List<Genstand> genstande = getGenstande(pDato);
        if (genstande != null){
            for (Genstand gn : genstande) {
                rels = gn.getKlausulRelationerGldFremtidige(pDato);
                if (rels != null){
                    for (KlausulholderKlausultype rel : rels){
                        if (rel.isGld(pDato))
                            retur.add(rel);
                    }
                }
                Produkt[] daekningerGld = gn.getDaekningerGld(pDato);
                if (daekningerGld != null){
                    for (Produkt daekning : daekningerGld){
                        rels = daekning.getKlausulRelationerGldFremtidige(pDato);
                        if (rels != null){
                            for (KlausulholderKlausultype rel : rels) {
                                if (rel.isGld(pDato))
                                    retur.add(rel);
                            }
                        }
                    }
                }
            }
        }
        return retur;
    }

    public List<KlausulholderKlausultype> getKlausulRelationerAlleNiveauer() {
	    List<KlausulholderKlausultype> retur = new ArrayList<>();

        KlausulholderKlausultype[] rels = getKlausultypeAftaleInclAnnullerede();
        if (rels != null){
            for (KlausulholderKlausultype rel : rels){
                retur.add(rel);
            }
        }

        Genstand[] genstande = getGenstand();
        if (genstande != null){
            for (Genstand gn : genstande) {
                rels = gn.getKlausultypeGenstandInclAnnullerede();
                if (rels != null){
                    for (KlausulholderKlausultype rel : rels){
                        retur.add(rel);
                    }
                }
                Produkt[] daekningerGld = gn.getDaekninger();
                if (daekningerGld != null){
                    for (Produkt daekning : daekningerGld){
                        rels = daekning.getKlausultypeProduktInclAnnullerede();
                        if (rels != null){
                            for (KlausulholderKlausultype rel : rels) {
                                retur.add(rel);
                            }
                        }
                    }
                }
            }
        }
        return retur;
    }

	@Override
	public KlausulholderKlausultype[] getKlausulRelationerGldFremtidige(BigDecimal pDato) {
		return this.getBttpAftaleGldOgFremtidige(pDato);
	}
 
	// genererede accessmetoder til WebAdgang
	public WebAdgang[] getWebAdgang() {
		return (WebAdgang[]) DBServer.getInstance().getVbsf().get(this, "WebAdgang");
	}

	public void addWebAdgang(WebAdgang pWebAdgang) {
		PersistensService.addToCollection(this, "WebAdgang", pWebAdgang);
	}

	public void removeWebAdgang(WebAdgang oldWebAdgang) {
		PersistensService.removeFromCollection(this, "WebAdgang", oldWebAdgang);
	}

 

 // genererede accessmetoder til RPNRegulering
  public RPNRegulering[] getRPNRegulering()  {
    return (RPNRegulering[]) DBServer.getInstance().getVbsf().get(this, "RPNRegulering"); 
  }
  public void addRPNRegulering(RPNRegulering pRPNRegulering) {
	  PersistensService.addToCollection(this, "RPNRegulering", pRPNRegulering);
  }
  public void removeRPNRegulering(RPNRegulering oldRPNRegulering)  {
	  PersistensService.removeFromCollection(this, "RPNRegulering", oldRPNRegulering);
  }

 

 // genererede accessmetoder til RPNAftaleKode
  public RPNAftaleKode[] getRPNAftaleKode()  {
    return (RPNAftaleKode[]) DBServer.getInstance().getVbsf().get(this, "RPNAftaleKode"); 
  }
  public void addRPNAftaleKode(RPNAftaleKode pRPNAftaleKode) {
	  PersistensService.addToCollection(this, "RPNAftaleKode", pRPNAftaleKode);
  }
  public void removeRPNAftaleKode(RPNAftaleKode oldRPNAftaleKode)  {
	  PersistensService.removeFromCollection(this, "RPNAftaleKode", oldRPNAftaleKode);
  }

	@Override
	public List<DagbogProdukt> getDagbogProduktIkkeUdfoert() {
		List<DagbogProdukt> result = new ArrayList<DagbogProdukt>();
		Produkt[] daekninger = getProdukt();
		for (int i = 0; daekninger != null && i < daekninger.length; i++) {
	        DagbogProdukt[] dagbogProduktIkkeUdfoert = daekninger[i].getDagbogProduktIkkeUdfoert();
	        for (int j = 0; dagbogProduktIkkeUdfoert != null && j < dagbogProduktIkkeUdfoert.length; j++) {
	            result.add(dagbogProduktIkkeUdfoert[j]);
            }
        }
		return result;
	}

    @Override
    public List<DagbogProdukt> getDagbogProduktUdfoerteFremtidige(Opgavetype pOpgavetype, BigDecimal pDato) {
    	List<DagbogProdukt> result = new ArrayList<DagbogProdukt>();
		Produkt[] daekninger = getProdukt();
		for (int i = 0; daekninger != null && i < daekninger.length; i++) {
	        DagbogProdukt[] dagbogProduktIkkeUdfoert = daekninger[i].getDagbogProduktUdfoerteFremtidige(pOpgavetype, pDato);
	        for (int j = 0; dagbogProduktIkkeUdfoert != null && j < dagbogProduktIkkeUdfoert.length; j++) {
	            result.add(dagbogProduktIkkeUdfoert[j]);
            }
        }
		return result;
    }

    @Override
    public boolean hasAftaleFrekvens(BigDecimal pDato) {
    	AftaleFrekvens[] aftaleFrekvens = getAftaleFrekvens(pDato);
    	return aftaleFrekvens != null && aftaleFrekvens.length == 1; 
    }

    @Override
    public List<DagbogProdukt> getDagbogProduktIPeriode(Opgavetype pOpgavetype, BigDecimal pFraDato, BigDecimal pTilDato) {
    	List<DagbogProdukt> result = new ArrayList<DagbogProdukt>();
		Produkt[] daekninger = getProdukt();
		for (int i = 0; daekninger != null && i < daekninger.length; i++) {
	        DagbogProdukt[] dagbogProdukter = daekninger[i].getDagbogProduktIPeriode(pOpgavetype, pFraDato, pTilDato);
	        for (int j = 0; dagbogProdukter != null && j < dagbogProdukter.length; j++) {
	            result.add(dagbogProdukter[j]);
            }
        }
		return result;
    }

    @Override
    public boolean isOverfoeresTilNaersikring() {
    	if(!dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.HAR_ADMINISTRATION_AF_ARBEJDSSKADER.isPresent()) {
    		if(isArbejdsskadeforsikring()) {
    			return true;
    		}
    	}
	    return false;
    }

	/**
	 * @return true hvis der er tale om en arbejdsskadeforsikring eller frivillig arbejdsskadeforsikring.
	 */
	public boolean isArbejdsskadeforsikring() {
		if(getAftaletypeKortBenaevnelse().equals(Aftaletype.NE_ARBEJDSSKADEFORSKIRING) || getAftaletypeKortBenaevnelse().equals(Aftaletype.NE_FSE_FRIVILLIG_ARBEJDSULYKKE) ||
			getAftaletypeKortBenaevnelse().equals(Aftaletype.NO_ARBEJDSSKADEFORSKIRING) || getAftaletypeKortBenaevnelse().equals(Aftaletype.NO_FSE_FRIVILLIG_ARBEJDSULYKKE)) {
			return true;
		}
		return false;
	}

    public boolean harAftaleForsikringstype(Aftaletype.Forsikringstype pForsikringstype) {
	    return this.getAftaletypeKortBenaevnelse().trim().compareTo(pForsikringstype.getKortBenaevnelse())== 0;
    }

	public boolean isOverfoertNaersikringNyGl() {
		final Opgavetype opgtp = OpgavetypeImpl.getOpgavetype(Opgavetype.NAERSIKRING_OVERFOERSEL_OPRET);

		boolean overfoertNy = isOverfoertNaersikringNy();
		if (overfoertNy)
			return true;

		final DagbogProdukt[] dagbogProdukts = getDagbogProdukt(opgtp, null, -1, Boolean.TRUE);
		if (dagbogProdukts != null && dagbogProdukts.length > 0)
			return true;

		return false;
	}

	public boolean isOverfoertNaersikringNy() {
		OQuery qry = new OQuery(OpfoelgAftaleOverfoerImpl.class);
		qry.add(getId(), "Aftale");
		qry.add(GaiaConst.JA, "behandlet");
		// skal også være kvitteret slut.  == GaiaConst.JA
		qry.setMaxCount(1);
		final OpfoelgAftaleOverfoerImpl[] logninger = (OpfoelgAftaleOverfoerImpl[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		return logninger != null && logninger.length > 0;
	}

	@Override
    public BigDecimal getForsikringssum(BigDecimal pDato) {
    	/**
    	 * Denne definition er anbefalet af HS/AJ primo 2013 til brug ved udtræk til NOR.
    	 * TODO Hvis den holder hele vejen rundt, bør den optimeres og dokumenteres bedre.
    	 * SHS er enig i algoritmmen, i betydningen "Forsikringssum" <> den teoretiske max. risiko.
    	 * 
    	 */
    	BigDecimal faktDato = getOpgDatoKorrigeret(pDato);
    	ReaRisikoTp rrtp = (ReaRisikoTp) DBServer.getInstance().getVbsf().lookupRegelType(ReaRisikoTpImpl.class, "BRAND");
    	
    	OQuery qry = new OQuery(RrTOrAfImpl.class);
    	qry.add(getId(), "Aftale");
    	qry.add(rrtp.getId(), "ReaRisikoTp");
    	qry = DBServer.getInstance().getVbsf().getGldQuery(qry,  faktDato);
    	qry.addOrder("emlsum", OQuery.DESC);
    	qry.setMaxCount(1);
    	RrTOrAfImpl[] queryExecute = (RrTOrAfImpl[]) DBServer.getInstance().getVbsf().queryExecute(qry);
    	if (queryExecute != null && queryExecute.length > 0)
    		return queryExecute[0].getEmlsum();
    	
    	// Så tager vi bare den højeste uanset rrtp
    	qry = new OQuery(RrTOrAfImpl.class);
    	qry.add(getId(), "Aftale");
    	qry = DBServer.getInstance().getVbsf().getGldQuery(qry,  faktDato);
    	qry.addOrder("emlsum", OQuery.DESC);
    	qry.setMaxCount(1);
    	queryExecute = (RrTOrAfImpl[]) DBServer.getInstance().getVbsf().queryExecute(qry);
    	if (queryExecute != null && queryExecute.length > 0)
    		return queryExecute[0].getEmlsum();
    	
    	return BigDecimal.ZERO;
    	
    }

    public boolean isFornyelsesBlokeret(BigDecimal pDato) {
    	if (!RykkerKonsekvensRegelImpl.harFornyelsesBlokeretRegel()) {
    		return false;
    	}
    	// Den første den bedste afregning med udestående der er i rykkerforløb efter første rykker blokerer
    	Afregning[] afregningerGrundTab = this.getAfregning();  // todo kan give mange hits
    	if (afregningerGrundTab == null) {
    		return false;
    	}
    	for (Afregning afregningGrund : afregningerGrundTab) {
    		if (!(afregningGrund.isUdlignet()) && afregningGrund.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
    			Afregning topAfregning = afregningGrund.getTopAfregning();
    			if (topAfregning.isRykket()) {
    				if (topAfregning.getRykkertype().getArfmRktp() == null) {
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }
    
    public XMLSerialiseringsData getBrandXMLSerialiseringsData(){
    	boolean harDebitorgrupperFlere = dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.HAR_DEBITORGRUPPER_FLERE.isPresent();
    	if(harDebitorgrupperFlere){
    		XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
    		xmlSerialiseringsData.setBenaevnelse(FasteTekster.BRAND.getBenaevnelseEkstern());
    		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.BRAND.getKortBenaevnelse());
    		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.TEKST);
			if ("FY".contains(DBServer.getInstance().getDatabasePrefixExclMiljoe())) {
				xmlSerialiseringsData.setVaerdiTekst(ConcordiaSelskabEnum.getSelskabsBrandTekst(this));
				return xmlSerialiseringsData;
			}
		}
    	return null;
    }
    
    public XMLSerialiseringsData getAarspraemieSerialiseringsData(BigDecimal pDato){
    	XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
		xmlSerialiseringsData.setBenaevnelse(FasteTekster.AARSPRAEMIE.getBenaevnelseEkstern());
		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.AARSPRAEMIE.getKortBenaevnelse());
		xmlSerialiseringsData.setVaerdiBD(this.getNettoPraemieInklOpkTillaeg(pDato));
		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.BELOEB);
		return xmlSerialiseringsData;
    }

	public XMLSerialiseringsData getAarspraemieInklStatsafgiftSerialiseringsData(BigDecimal pDato){
		XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
		xmlSerialiseringsData.setBenaevnelse(FasteTekster.AARSPRAEMIEINKLSTATSAFGIFT.getBenaevnelseEkstern());
		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.AARSPRAEMIEINKLSTATSAFGIFT.getKortBenaevnelse());
		xmlSerialiseringsData.setVaerdiBD(this.getAarspraemieInklStatsafgift(pDato));
		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.BELOEB);
		return xmlSerialiseringsData;
	}

	public XMLSerialiseringsData getAarsafgifterSerialiseringsData(BigDecimal pDato){
		XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
		xmlSerialiseringsData.setBenaevnelse(FasteTekster.AARSOMKOSTNINGER.getBenaevnelseEkstern());
		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.AARSOMKOSTNINGER.getKortBenaevnelse());
		xmlSerialiseringsData.setVaerdiBD(this.getAftaleOmkostningerInklStatsafgift(pDato));
		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.BELOEB);
		return xmlSerialiseringsData;
	}
 
    public BigDecimal getAarsMiljoebidrag(BigDecimal pDato) {
    	BigDecimal svar = BigDecimal.ZERO;
    	if (findesUdfoertBeregning(pDato)) {
    		Produkt[] daekningerGld = getDaekningerGld(pDato);
    		if (isFiktivBeregning(pDato)) {
    			for (int i = 0; daekningerGld != null &&  i < daekningerGld.length; i++) {
    				FiktivProduktOmkostningstype[] fiktivProduktOmkostningstyper = daekningerGld[i].getFiktivProduktOmkostningstype(pDato);
    				for (int j = 0; fiktivProduktOmkostningstyper != null && j < fiktivProduktOmkostningstyper.length; j++) {
    					if (fiktivProduktOmkostningstyper[j].getOmkostningstype().isMiljoeBidrag()){
    						svar = svar.add(fiktivProduktOmkostningstyper[j].getOmkostningsbeloeb());
    					}
    				} 
    			}

    		}else{
    			for (int i = 0; daekningerGld != null &&  i < daekningerGld.length; i++) {
    				ProduktOmkostningstype[] produktOmkostningstyper = daekningerGld[i].getProduktOmkostningstype(pDato);
    				for (int j = 0; produktOmkostningstyper != null && j < produktOmkostningstyper.length; j++) {
    					if (produktOmkostningstyper[j].getOmkostningstype().isMiljoeBidrag()){
    						svar = svar.add(produktOmkostningstyper[j].getOmkostningsbeloeb());
    					}
    				} 
    			}
    		}
    	}

    	return svar;
    }
    
    public BigDecimal getAarsSkadeforsikringsafgift(BigDecimal pDato) {
    	BigDecimal svar = BigDecimal.ZERO;
    	if (findesUdfoertBeregning(pDato)) {
    		Produkt[] daekningerGld = getDaekningerGld(pDato);
    		if (isFiktivBeregning(pDato)) {
    			for (int i = 0; daekningerGld != null &&  i < daekningerGld.length; i++) {
    				FiktivProduktOmkostningstype[] fiktivProduktOmkostningstyper = daekningerGld[i].getFiktivProduktOmkostningstype(pDato);
    				for (int j = 0; fiktivProduktOmkostningstyper != null && j < fiktivProduktOmkostningstyper.length; j++) {
    					if (fiktivProduktOmkostningstyper[j].getOmkostningstype().isSkadeforsikringsafgift()){
    						svar = svar.add(fiktivProduktOmkostningstyper[j].getOmkostningsbeloeb());
    					}
    				} 
    			}
    		}else{
    			for (int i = 0; daekningerGld != null &&  i < daekningerGld.length; i++) {
    				ProduktOmkostningstype[] produktOmkostningstyper = daekningerGld[i].getProduktOmkostningstype(pDato);
    				for (int j = 0; produktOmkostningstyper != null && j < produktOmkostningstyper.length; j++) {
    					if (produktOmkostningstyper[j].getOmkostningstype().isSkadeforsikringsafgift()){
    						svar = svar.add(produktOmkostningstyper[j].getOmkostningsbeloeb());
    					}
    				} 
    			}
    		}
    	}
    	
    	return svar;
    }

 // genererede accessmetoder til PBSKundenummerskift
  public PBSKundenummerskift[] getPBSKundenummerskift()  {
    return (PBSKundenummerskift[]) DBServer.getInstance().getVbsf().get(this, "PBSKundenummerskift"); 
  }
  public void addPBSKundenummerskift(PBSKundenummerskift pPBSKundenummerskift) {
	  PersistensService.addToCollection(this, "PBSKundenummerskift", pPBSKundenummerskift);
  }
  public void removePBSKundenummerskift(PBSKundenummerskift oldPBSKundenummerskift)  {
	  PersistensService.removeFromCollection(this, "PBSKundenummerskift", oldPBSKundenummerskift);
  }

	/**
	 * Finder alle framtidige tarif datoer > pDato  
	 */
	public BigDecimal[] findFremtidigeTariferingsDatoer(BigDecimal pStartDato) {
		DagbogProdukt[] dgpd = findFremtidigeTariferingsOpgaver(pStartDato);

		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		for (int i = 0; dgpd != null && i < dgpd.length; i++) {
			if (!result.contains(dgpd[i].getGld()))
				result.add(dgpd[i].getGld());
		}
		Collections.sort(result);
		return ContainerUtil.toArray(result);
	}

	public DagbogProdukt[] findFremtidigeTariferingsOpgaver(BigDecimal pStartDato) {
		OQuery qry = QueryService.queryCreate(DagbogProduktImpl.class);
		qry.add(pStartDato, "gld", OQuery.GREATER);
		qry.add(OpgavetypeImpl.getOpgavetype(OpgavetypeImpl.TARIFERING).getId(), "Opgavetype", OQuery.EQUAL);
		qry.addJoin(DagbogProduktImpl.class.getName() + ".Produkt", ProduktImpl.class.getName() + ".produktId");
		qry.add(aftaleId, ProduktImpl.class.getName() + ".Aftale");

		return (DagbogProdukt[]) DBServer.getInstance().getVbsf().queryExecute(qry);
	}

	public List<BigDecimal> getFremtidigeUdfoerteFornyelseOgDelopkraevningsdatoer(BigDecimal pDato) {
		OQuery qry = QueryService.queryCreate(DagbogAftaleImpl.class);
		qry.add(BigDecimal.ZERO, "udfoertden", OQuery.NOT_EQUAL);
		qry.add(pDato, "gld", OQuery.GREATER);
		qry.add(OpgavetypeImpl.getOpgavetype(OpgavetypeImpl.FORNYELSE).getId(), "Opgavetype", OQuery.EQUAL, OQuery.AND, OQuery.BEG_PAR);
		qry.add(OpgavetypeImpl.getOpgavetype(OpgavetypeImpl.DELOPK).getId(), "Opgavetype", OQuery.EQUAL, OQuery.OR, OQuery.END_PAR);
		qry.add(aftaleId,  "Aftale");

		DagbogAftale[] dgaf = (DagbogAftale[]) DBServer.getInstance().getVbsf().queryExecute(qry);

		ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
		for (int i = 0; dgaf != null && i < dgaf.length; i++) {
			if (!result.contains(dgaf[i].getGld()))
				result.add(dgaf[i].getGld());
		}
		Collections.sort(result);
		return result;
		
	}
	
	public XMLSerialiseringsData getForsikringIdXMLSerialiseringsData() {
		XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
		xmlSerialiseringsData.setBenaevnelse(FasteTekster.FORSIKRING_ID.getBenaevnelseEkstern());
		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.FORSIKRING_ID.getKortBenaevnelse().trim());
		xmlSerialiseringsData.setVaerdiTekst(getId());
		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.TEKST);
		return xmlSerialiseringsData;
	}

	public XMLSerialiseringsData getReguleringPaaNettetSerialiseringsData() {
		XMLSerialiseringsData xmlSerialiseringsData = new XMLSerialiseringsData();
		xmlSerialiseringsData.setBenaevnelse(FasteTekster.REGULERING_PAA_NETTET_KODEORD.getBenaevnelseEkstern());
		xmlSerialiseringsData.setKortbenaevnelse(FasteTekster.REGULERING_PAA_NETTET_KODEORD.getKortBenaevnelse().trim());
		RPNService rpnService = RPNServiceImpl.newInstance();
		xmlSerialiseringsData.setVaerdiTekst(rpnService.findAdgangskode(this));
		xmlSerialiseringsData.setVaerditype(XMLSerialiseringsData.VAERDITYPE.TEKST);
		return xmlSerialiseringsData;
	}

	public Aftale getForsikringOprindeligeFraThisTilbud() {
		GsproMapningService gsproMapningService = new GsproMapningService();
		gsproMapningService.setModelObjekt_(this);
		GsproMapning gsproMapning = gsproMapningService.getGsproMapningKunAktive();
		if (gsproMapning != null) {
			Aftale aftaleOprindelig = gsproMapning.getAftaleFra();
			return aftaleOprindelig;
		}
		return null;
	}

	public boolean isKopiTilbudAktivt() {
		return harKopiTilbudMedEllerUdenInaktive(false);
	}

	public boolean isKopiTilbudAktivtEllerInAktiv() {
		if(!harKopiTilbudMedEllerUdenInaktive(true)) {
			return isKopiTilbudAktivt();
		}
		return true;
	}
	private boolean harKopiTilbudMedEllerUdenInaktive(boolean pInclInaktive) {

		if (!dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.HAR_KOEBT_FORSIKRING_KOPIER_TIL_TILBUD_OG_TILBAGE.isPresent()) {
			return false;
		}

		GsproMapningService gsproMapningService = new GsproMapningService();
		gsproMapningService.setModelObjekt_(this);
		GsproMapning gsproMapningAktive = gsproMapningService.getGsproMapningKunAktive();
		if(gsproMapningAktive != null) {
			return true;
		}
		if(pInclInaktive) {
			GsproMapning[] gsproMapningInAktive = gsproMapningService.getGsproMapningAktiveOgInaktive();
			if(gsproMapningInAktive != null) {
				return true;
			}
		}
		return false;
	}
	public GsproMapning getGsproMapning() {

		if (!dk.gensam.gaia.util.funktionalitet.AlleTillaegsfunktionaliteter.HAR_KOEBT_FORSIKRING_KOPIER_TIL_TILBUD_OG_TILBAGE.isPresent()) {
			return null;
		}

		GsproMapningService gsproMapningService = new GsproMapningService();
		gsproMapningService.setModelObjekt_(this);
		GsproMapning gsproMapning = gsproMapningService.getGsproMapningKunAktive();
		if(gsproMapning != null) {
			return gsproMapning;
		}
		return null;
	}

 

 // genererede accessmetoder til UdtraekInformationssystemAftale
  public UdtraekInformationssystemAftale[] getUdtraekInformationssystemAftale()  {
    return (UdtraekInformationssystemAftale[]) DBServer.getInstance().getVbsf().get(this, "UdtraekInformationssystemAftale"); 
  }
  public void addUdtraekInformationssystemAftale(UdtraekInformationssystemAftale pUdtraekInformationssystemAftale) {
	  PersistensService.addToCollection(this, "UdtraekInformationssystemAftale", pUdtraekInformationssystemAftale);
  }
  public void removeUdtraekInformationssystemAftale(UdtraekInformationssystemAftale oldUdtraekInformationssystemAftale)  {
	  PersistensService.removeFromCollection(this, "UdtraekInformationssystemAftale", oldUdtraekInformationssystemAftale);
  }

	public BigDecimal getSenesteRevideringPaaDato(BigDecimal pDato) {
		BigDecimal result = null;
		if (pDato != null) {
			AftaleAfegn[] aftaleAfegn = getAftaleAfegn(AftaleegngrpImpl.getEgenskabKortnavn(Aftaleegngrp.Aftalefelt.FORSIKRINGAENDRETPRe.getKortBenaevnelse()));
			if (aftaleAfegn != null) {
				for (int i = 0;i < aftaleAfegn.length; i++) {
					Egenskab egenskab = aftaleAfegn[i].getEgenskab();
					String s = egenskab.formatToDisplay();
					if (s.equals(Datobehandling.format(pDato))) {
						BigDecimal tmpDato = aftaleAfegn[i].getOprDato();
						if (result == null || (result.compareTo(tmpDato) < 0)) {
							result = tmpDato;
						}
					}
				}
			}
		}
		return result;
	}

	public Date getGaeldendeFraRegistreringstidspunkt(BigDecimal pDato) {
		Date result = null;
		if (pDato != null) {
			AftaleAfegn[] aftaleAfegn = getAftaleAfegn(AftaleegngrpImpl.getEgenskabKortnavn(Aftaleegngrp.Aftalefelt.FORSIKRINGAENDRETPRe.getKortBenaevnelse()));
			if (aftaleAfegn != null) {
				for (int i = 0;i < aftaleAfegn.length; i++) {
					Egenskab egenskab = aftaleAfegn[i].getEgenskab();
					String s = egenskab.formatToDisplay();
					if (s.equals(Datobehandling.format(pDato))) {
						Date tmpDate = aftaleAfegn[i].getOprDatoTid();
						if (result == null || (result.compareTo(tmpDate) < 0)) {
							result = tmpDate;
						}
					}
				}
			}

			AftaleStatustype aftaleStatustypenMedGld = getAftaleStatustypenMedGld(pDato);
			if (aftaleStatustypenMedGld != null) {
				Date tmpDate = aftaleStatustypenMedGld.getOprDatoTid();
				if(result == null || (result.compareTo(tmpDate) < 0)) {
					result = tmpDate;
				}
			}

			DagbogProdukt[] dagbogProduktTarifopgave = getDagbogProduktTarifopgave(pDato);
			for (int i = 0;dagbogProduktTarifopgave != null && i < dagbogProduktTarifopgave.length; i++) {
				Date tmpDate = dagbogProduktTarifopgave[i].getUdfoertDatoTid();
				if(tmpDate != null) {
					if(result == null || (result.compareTo(tmpDate) < 0)) {
						result = tmpDate;
					}
				} else {
					tmpDate = dagbogProduktTarifopgave[i].getOprDatoTid();
					if(tmpDate != null) {
						if (result == null || (result.compareTo(tmpDate) < 0)) {
							result = tmpDate;
						}
					}
				}
			}

			DagbogAftale[] dagbogAftaleFornyelseEllerDelopkraevning = getDagbogAftaleFornyelseEllerDelopkraevning(pDato, true);
			for (int i = 0;dagbogAftaleFornyelseEllerDelopkraevning != null && i < dagbogAftaleFornyelseEllerDelopkraevning.length; i++) {
				Date tmpDate = dagbogAftaleFornyelseEllerDelopkraevning[i].getUdfoertDatoTid();
				if(tmpDate != null) {
					if(result == null || (result.compareTo(tmpDate) < 0)) {
						result = tmpDate;
					}
				} else {
					tmpDate = dagbogAftaleFornyelseEllerDelopkraevning[i].getOprDatoTid();
					if(tmpDate != null) {
						if(result == null || (result.compareTo(tmpDate) < 0)) {
							result = tmpDate;
						}
					}
				}
			}

			Aftalehaendelse aftalehaendelseNyeste = getAftalehaendelseNyeste(pDato, false);
			if(aftalehaendelseNyeste != null) {
				Date tmpDate = aftalehaendelseNyeste.getOprDatoTid();
				if(result == null || (result.compareTo(tmpDate) < 0)) {
					result = tmpDate;
				}
			}

			// 30408 bagstopper for bla. aftale status der kopieres fra tilbud til forsikring.
			if(result == null) {
				AftaleStatustype aftaleStatustypenSenestOprettetMedGld = getAftaleStatustypeSenestOprettetMedGld(pDato);
				if (aftaleStatustypenSenestOprettetMedGld != null) {
					result = aftaleStatustypenSenestOprettetMedGld.getOprDatoTid();
				}
			}

			// 31116 Ophør
			if(result == null) {
				aftaleAfegn = getAftaleAfegn(AftaleegngrpImpl.getEgenskabKortnavn(Aftaleegngrp.Aftalefelt.SAGSBEHANDLERe.getKortBenaevnelse()));
				if (aftaleAfegn != null) {
					for (int i = 0; i < aftaleAfegn.length; i++) {
						if (aftaleAfegn[i].getGld().equals(pDato)) {
							Date tmpDate = aftaleAfegn[i].getOprDatoTid();
							if (result == null || (result.compareTo(tmpDate) < 0)) {
								result = tmpDate;
							}
						}
					}
				}
			}

		}
		return result;
	}

	public HenvisningAftale getHenvisningAftale() {
		HenvisningAftaleImpl[] henvisningAftales = (HenvisningAftaleImpl[]) DBServer.getInstance().getVbsf().get(this, "HenvisningAftale");
		return henvisningAftales != null && henvisningAftales.length > 0 ? henvisningAftales[0] : null;
	}

	@Override
	public Set<ReaRisikoTp> collectReaRisikotyper(BigDecimal gld) {
		Set<ReaRisikoTp> retur = new HashSet<>();
		final Produkt[] daekningerGld = getDaekningerGld(gld);
		if (daekningerGld != null){
			for (Produkt daekning : daekningerGld){
				final Produkttype produkttype = daekning.getProdukttype();
				final ReaRisikoTp[] reaRisikoTpGld = produkttype.getReaRisikoTpGld(gld);
				if (reaRisikoTpGld != null){
					for (ReaRisikoTp rrtp : reaRisikoTpGld){
						if (rrtp.isMedOmraadetypeForsikringssted(gld))
							retur.add(rrtp);
					}
				}
			}
		}
		return retur;
	}

	public Dagbog getDagbogen() {
		OQuery qry = new OQuery(DagbogAftaleUpdate.class);
		qry.add(getId(), "Aftale");
		qry.setMaxCount(1);
		final DagbogAftaleUpdate[] random = (DagbogAftaleUpdate[]) DBServer.getInstance().getVbsf().queryExecute(qry);
		if (random == null || random.length < 1)
			throw new DatafejlException("Der skal være en dagbogaftale på aftale " + this.toString());
		return random[0].getDagbog();
	}

	@Override
	public void udfoerYdelsesAfspejling() {
		final Genstand[] genstande = getGenstand();
		if (genstande == null)
			return;
		final boolean startedTx = PersistensService.transactionBegin();

		DBServer.getInstance().getVbsf().markCollectionDirty(this, GenstandYdtpAng.GenstandYdtpAng);
		// 31893 når der i transaktionen er slettet xxydtang og ingen nye, er cachen ude af synk, så det her er et rigtigt hack
		final ProduktYdtpAng[] alleDaekningsydelserInclAfspejlingerAftalen = getProduktYdtpAng(null, null);

		for (Genstand genstand : genstande){
			final Produkt[] daekninger = genstand.getProdukt();
			if (daekninger != null){
				for (Produkt daekning : daekninger) {
					if (daekning.isAnnulleret())
						continue;
					afspejlTil(daekning, alleDaekningsydelserInclAfspejlingerAftalen);
				}
			}
		}
		if (startedTx)
			PersistensService.transactionCommit();
	}

	/**
	 *
	 * @param daekning daekning
	 * @param alleDaekningsydelserInclAfspejlingerAftalen
	 */
	private void afspejlTil(Produkt daekning, ProduktYdtpAng[] alleDaekningsydelserInclAfspejlingerAftalen) {
		final AftalekompTypeYdtpIF[] aftalekompTypeYdtp = daekning.getAftalekomponentType().getAftalekompTypeYdtp();
		if (aftalekompTypeYdtp == null)
			return;
		for (AftalekompTypeYdtpIF regelRel : aftalekompTypeYdtp){
			final String fraentitet = regelRel.getFraentitet();
			if (!fraentitet.isEmpty() && !regelRel.isOphoert(daekning.getGld())){
				// fraentitet skal være AFTALE eller GENSTAND
				final Ydelsestype ydelsestype = regelRel.getYdelsestype();
				if (ydelsestype.isAnnulleret())
					continue;
				if (fraentitet.startsWith(AftalekompTypeYdtpIF.SPEJL_AFTALE)) {
					afspejl(daekning.getAftalen(), daekning, regelRel.getYdelsestype(), alleDaekningsydelserInclAfspejlingerAftalen);
				}
				if (fraentitet.startsWith(AftalekompTypeYdtpIF.SPEJL_GENSTAND)) {
					afspejl(daekning.getGenstanden(), daekning, regelRel.getYdelsestype(), alleDaekningsydelserInclAfspejlingerAftalen);
				}

			}
		}
	}

	private void afspejl(AftalekomponentIF genstandAftale, Produkt daekning, Ydelsestype pYdelsestype, ProduktYdtpAng[] alleDaekningsydelserInclAfspejlingerAftalen) {
		List<ProduktYdtpAng>  ydelsesAngivelserTilLst = ContainerUtil.asList(alleDaekningsydelserInclAfspejlingerAftalen);

		final Aftale aftalen = genstandAftale.getAftalen();
		BigDecimal stopklods = aftalen.getAeldsteAendringsDatoTilladt();

		final AftalekompYdtpAngivelseIF[] ydelsesAngivelserFra = genstandAftale.getYdelsesAngivelser();
		if (ydelsesAngivelserFra != null){
			for (AftalekompYdtpAngivelseIF fraRel : ydelsesAngivelserFra) {
				if (daekning.isOphUdfyldt() &&
						fraRel.isFremtidig(daekning.getOph()))
					continue;
				if (fraRel.isOphUdfyldt() &&
					daekning.isFremtidig(fraRel.getOph()))
					continue;

				if (fraRel.getYdelsestype().equals(pYdelsestype)) {
					ProduktYdtpAngInclAfspejlingerImpl newInstImpl = fraRel.copyToAfspejlingAng(fraRel, daekning);

					final int i = ydelsesAngivelserTilLst.indexOf(newInstImpl);
					boolean gemt = false;
					if (i >= 0){
						ProduktYdtpAng persistedInstance = ydelsesAngivelserTilLst.get(i);
						ydelsesAngivelserTilLst.remove(i);
						if (!newInstImpl.isSame(persistedInstance)) {
							newInstImpl.copyAttributes(newInstImpl, persistedInstance);
							PersistensService.gem(persistedInstance);
							gemt = true;
						}
					} else {
						// Ikke tidligere afspejlet - hvorfor sker det ved test uden ændringer ?
						// er reglen nyere end source?
						if (newInstImpl.isOphoert(stopklods)){
							// Ikke helt rationelt, da afspejlinger også anvendes ved Notes-udsøgning, men
							// det er et værn mod mange gamle mangler lige nu.
						}   else {
							PersistensService.gem(newInstImpl);
							gemt = true;
						}
					}
					if (gemt)
						opretProduktYdtp(newInstImpl);
				}
			}
		}
		if (ydelsesAngivelserTilLst != null && !ydelsesAngivelserTilLst.isEmpty()){
			for (AftalekompYdtpAngivelseIF rel : ydelsesAngivelserTilLst) {
				if (rel.getYdelsestype().equals(pYdelsestype) &&
						rel.getAftalekomponent().equals(daekning)) {
					 // Kan kun betyde, at den skal slettes.
					PersistensService.slet(rel);
					log_.info("Har slettet " + rel.toString());
				}
			}
			ydelsesAngivelserTilLst.clear();
		}

	}

	private void opretProduktYdtp(ProduktYdtpAngInclAfspejlingerImpl newInstImpl) {
		ProduktYdtpImpl rel = PersistensService.opret(ProduktYdtpImpl.class);
		rel.setProdukt(newInstImpl.getProdukt());
		rel.setGld(newInstImpl.getProdukt().getGld());
		rel.setYdelsestype(newInstImpl.getYdelsestype());
		if (!DBServer.getInstance().getVbsf().existsInDb(rel))
			PersistensService.gem(rel);
	}

	@Override
	public void sletNettoPraemiePrDaekningIPerioden(BigDecimal pFraDato, BigDecimal pTilDato) {
		NettoPraemiePrDaekning[] nettoPraemiePrDaekningTab = getNettoPraemiePrDaekning(null);
		if (nettoPraemiePrDaekningTab == null) {
			return;
		}
		for (NettoPraemiePrDaekning nettoPraemiePrDaekning : nettoPraemiePrDaekningTab) {
			if (nettoPraemiePrDaekning.getGld().compareTo(pFraDato) == 0) {
				nettoPraemiePrDaekning.getProdukt().removeNettoPraemiePrDaekning(nettoPraemiePrDaekning);
				this.removeNettoPraemiePrDaekning(nettoPraemiePrDaekning);
				PersistensService.delete(nettoPraemiePrDaekning);
			}
			else {
				if (nettoPraemiePrDaekning.isGld(pFraDato)) {
					nettoPraemiePrDaekning.setOph(Datobehandling.datoPlusMinusAntal(pFraDato, -1));
					PersistensService.save(nettoPraemiePrDaekning);
				}
			}
		}
	}

	@Override
	public void sletFiktivStormflodIPerioden(BigDecimal pFraDato, BigDecimal pTilDato) {
		FiktivStormflod[] fiktivStormflodTab = getFiktivStormflod();
		if (fiktivStormflodTab == null) {
			return;
		}
		for (FiktivStormflod fiktivStormflod : fiktivStormflodTab) {
			if (fiktivStormflod.getGld().compareTo(pFraDato) == 0) {
				this.removeFiktivStormflod(fiktivStormflod);
				PersistensService.delete(fiktivStormflod);
			}
			else {
				if (fiktivStormflod.isGld(pFraDato)) {
					fiktivStormflod.setOph(Datobehandling.datoPlusMinusAntal(pFraDato, -1));
					PersistensService.save(fiktivStormflod);
				}
			}
		}
	}

    @Override
    public void sletFiktivAarsafgiftIPerioden(BigDecimal pFraDato, BigDecimal periodeSlutDato_) {
        FiktivAftaleAarsAfgift[] fiktivAftaleAarsAfgifts = getFiktivAftaleAarsAfgift();
        if (fiktivAftaleAarsAfgifts == null) {
            return;
        }
        for (FiktivAftaleAarsAfgift fiktivAftaleAarsAfgift : fiktivAftaleAarsAfgifts) {
            if (fiktivAftaleAarsAfgift.getGld().compareTo(pFraDato) == 0) {
                this.removeFiktivAftaleAarsAfgift(fiktivAftaleAarsAfgift);
                PersistensService.delete(fiktivAftaleAarsAfgift);
            }
            else {
                if (fiktivAftaleAarsAfgift.isGld(pFraDato)) {
                    fiktivAftaleAarsAfgift.setOph(Datobehandling.datoPlusMinusAntal(pFraDato, -1));
                    PersistensService.save(fiktivAftaleAarsAfgift);
                }
            }
        }

    }

    public ReasProduktAdresse[] getReasProduktAdresse()  {
		return (ReasProduktAdresse[]) DBServer.getInstance().getVbsf().get(this, "ReasProduktAdresse");
	}

	@Override
	public ReasFordringWork getReasFordringWorkNyesteTil() {
		ReasFordringWork nyesteTil = null;
		BigDecimal nyesteTilDato = GaiaConst.FIKTIV_DATO_99991231;
		Produkt[] daekninger = getProdukt();
		for (Produkt daekning : daekninger) {
			ReasFordringWork reasFordringWorkNyesteTil = daekning.getReasFordringWorkNyesteTil();
			if (reasFordringWorkNyesteTil != null) {
				BigDecimal periodetil = reasFordringWorkNyesteTil.getPeriodetil();
				if (periodetil.compareTo(nyesteTilDato) < 0) {
					nyesteTilDato = periodetil;
					nyesteTil = reasFordringWorkNyesteTil;
				}
			}
		}
		return nyesteTil;
	}

	@Override
	public ReasFordringWork getReasFordringWork(BigDecimal pGld) {
		Produkt[] daekninger = getProdukt();
		for (Produkt daekning : daekninger) {
			ReasFordringWork reasFordringWorkGld = daekning.getReasFordringWork(pGld);
			if (reasFordringWorkGld != null) {
				return reasFordringWorkGld;
			}
		}
		return null;
	}

	@Override
	public BigDecimal findSenesteRegistredePraemieSlutdato() {
		// Skaf alle ReFrWk for hele aftalen og studer dem i Id-orden bagfra
		// Sidst udlagte dækning får lov at afgøre sagen
		// Aktive dækninger giver altid det retvisende billede af seneste præmieperiode, så vi ser kun på
		// ophørte dækninger hvis der ikke er andre muligheder
		List<ReasFordringWork> reasFordringWorkList = getReasFordringWorksKunPraemieRistorno(true);
		if (reasFordringWorkList.isEmpty()) {
			reasFordringWorkList = getReasFordringWorksKunPraemieRistorno(false);
			if (reasFordringWorkList.isEmpty()) {
				return Datobehandling.datoPlusMinusAntal(this.getGld(), -1);
			}
		}
		Collections.sort(reasFordringWorkList, new ModelObjektIdComparator());
		return reasFordringWorkList.get(reasFordringWorkList.size() - 1).getProdukt().findSenesteRegistreredePraemieSlutdato();
	}

	private List<ReasFordringWork> getReasFordringWorksKunPraemieRistorno(boolean pUndladOphoerteDaekninger) {
		List<ReasFordringWork> reasFordringWorkList = new ArrayList<ReasFordringWork>();
		Produkt[] daekningTab = this.getDaekninger();
		for (Produkt daekning : daekningTab) {
			List<ReasFordringWork> reasFordringWorkLstW = daekning.getReasFordringWorkKunPraemieRistorno();
			if (reasFordringWorkLstW == null || reasFordringWorkLstW.isEmpty()) {
				continue;
			}
			for (ReasFordringWork reasFordringWork : reasFordringWorkLstW) {
				if (pUndladOphoerteDaekninger && daekning.isOphUdfyldt()) {
					if (daekning.getOph().compareTo(reasFordringWork.getPeriodefra()) >= 0) {
						reasFordringWorkList.add(reasFordringWork);
					}
					continue;
				}
				reasFordringWorkList.add(reasFordringWork);
			}
		}
		return reasFordringWorkList;
	}

	@Override
	public BigDecimal findSidsteUdlagtePraemieSlutdato(BigDecimal pMaxDato) {
		BigDecimal slutDato = BigDecimal.ZERO;
		Produkt[] daekningTab = this.getDaekninger();
		if (daekningTab != null && daekningTab.length > 0){
			for (Produkt daekning : daekningTab) {
				BigDecimal sidsteUdlagtePraemieSlutdato = daekning.findSidsteUdlagtePraemieSlutdato(pMaxDato);
				if (sidsteUdlagtePraemieSlutdato != null && sidsteUdlagtePraemieSlutdato.compareTo(slutDato) > 0) {
					slutDato = sidsteUdlagtePraemieSlutdato;
				}
			}
		}
		return slutDato;
	}
//
//	@Override
//	public boolean harUdeståendeFrekvensSkift() {
//		BigDecimal senesteForventedeSlutdato = Datobehandling.datoPlusMinusAntal(this.getTidligsteIkkeUdfoertFornyelseEllerDelopkraevning(), -1);
//		if (senesteForventedeSlutdato == null || (this.isOphUdfyldt() && senesteForventedeSlutdato.compareTo(this.getOph()) > 0)) {
//			return false;
//		}
//		BigDecimal udlagtPraemieSlutDato_ = this.findSenesteRegistredePraemieSlutdato();
//		if (senesteForventedeSlutdato.compareTo(udlagtPraemieSlutDato_) == 0) {
//			return false;
//		}
//		return true;
//	}

	/**
	 * Non-persistent flag der bruges til at huske status på Nær-overførsel
	 */
	private boolean isOverfoertTemp = false;

	public boolean isOverfoertTemp() {
		return isOverfoertTemp;
	}

	public void setOverfoertTemp(boolean overfoertTemp) {
		this.isOverfoertTemp = overfoertTemp;
	}


	public BigDecimal getTidligstTilladteBonusRevidering() {
		String qryIn = "SELECT IDENT FROM produkt WHERE IDENT2 = '" + this.getAftaleId() + "'";
		OQuery qry = DBServer.getVbsfInst().queryCreate(ProduktBntpImpl.class);
		qry.add(qryIn, "Produkt", OQuery.IN);
		qry.addOrder("gld", OQuery.DESC);
		ProduktBntp[] produktBntpTab = (ProduktBntp[]) DBServer.getVbsfInst().queryExecute(qry);
		if (produktBntpTab == null || produktBntpTab.length == 0) {
			return this.getGld();
		}

		BigDecimal rtnDato = BigDecimal.ZERO;
		BigDecimal nyestePdbnDato = produktBntpTab[0].getGld();
		Bonustype bonustype = produktBntpTab[0].getBonustype();
		Produkt[] daekningerGld = this.getDaekningerGld(nyestePdbnDato);
		for (Produkt produkt : daekningerGld) {
			BigDecimal tidligsteDato = produkt.getTidligsteRevideringsdatoSammeBonusklasse(nyestePdbnDato, bonustype);
			if (tidligsteDato.compareTo(rtnDato) > 0) {
				rtnDato = tidligsteDato;
			}
		}
		return rtnDato;
	}

	@Override
	public boolean isVisesPaaMiniSider() {
		Egenskab felt = getFelt(Aftaleegngrp.Aftalefelt.VISMINESID);
		if(felt != null) {
			if(felt.formatToDisplay().equals(GaiaConst.JA_TEXT)) {
				return true;
			}
		}
		return false;
	}
 

 // genererede accessmetoder til SmsMO
  public SmsMO[] getSmsMO()  {
    return (SmsMO[]) DBServer.getInstance().getVbsf().get(this, "SmsMO"); 
  }
  public void addSmsMO(SmsMO pSmsMO) {
	  PersistensService.addToCollection(this, "SmsMO", pSmsMO);
  }
  public void removeSmsMO(SmsMO oldSmsMO)  {
	  PersistensService.removeFromCollection(this, "SmsMO", oldSmsMO);
  }

	@Override
	public List<Sms> getSmsser(BigDecimal oprDatoFra, SmsStatus... smsStatusser) {
		return Sms.readSmsser(s -> s
				.verbose(true)
				.aftale(this)
				.oprDatoFra(oprDatoFra)
				.status(Arrays.asList(smsStatusser)));
	}

    @Override
	public boolean isPoliceUdskriftFravalgt(BigDecimal dato){
		Aftalehaendelse aftalehaendelseNyeste = getAftalehaendelseNyeste(dato, Aftalehaendelse.HAENDELSESTYPE_AENDRING);
		if (aftalehaendelseNyeste == null) {
			aftalehaendelseNyeste = getAftalehaendelseNyeste(dato, Aftalehaendelse.HAENDELSESTYPE_NYTEGNING); // HOT-14130
		}
		if (aftalehaendelseNyeste != null) {
			return aftalehaendelseNyeste.isPoliceUdskriftFravalgt();
		}
        return false;
	}

	@Override
	public List<Aftale> getAlleAftalerMedSammePolicenummerExclTilbud() {
		List<Aftale> result = new ArrayList<>();
		result.add(this);
		Egenskab policenummerEg = getPolicenummerDirekte(getGld());
		if (policenummerEg != null) {
			EgenskabHolderEgenskab[] egenskabHolderEgenskabTab = policenummerEg.getEgenskabHolderEgenskab();
			for (EgenskabHolderEgenskab egenskabHolderEgenskab : egenskabHolderEgenskabTab) {
				Aftale aftale = (Aftale) egenskabHolderEgenskab.getEgenskabHolder();
				if (!(aftale.isTilbud()) && !(result.contains(aftale))) {
					result.add(aftale);
				}
			}
		}
		return result;
	}

    public final BigDecimal getTarifDatoFraFelt(boolean korrigerNaarData) {
        Egenskab tarifdato = getFelt(DBServer.getInstance().getRegelServer().getAftalegrp(Aftaleegngrp.Aftalefelt.TARIFDATO.getKortBenaevnelse()));
        if (tarifdato != null ){
            try {
                BigDecimal vendtDato = Datobehandling.skaermDatoTilBigDecimal(tarifdato.getBenaevnelseTrim(), "ddMMyyyy");
                if (korrigerNaarData && this.isFremtidig(vendtDato))
                    vendtDato = getGld();
                return vendtDato;
            } catch (ParseException e) {
                e.printStackTrace();
                throw new DatafejlException("Der skal være angivet en valid tarifDato");
            }
        }
        return null;
    }



 // genererede accessmetoder til SkadeAnmeldelseKlade
  public SkadeAnmeldelseKlade[] getSkadeAnmeldelseKlade()  {
    return (SkadeAnmeldelseKlade[]) DBServer.getInstance().getVbsf().get(this, "SkadeAnmeldelseKlade"); 
  }
  public void addSkadeAnmeldelseKlade(SkadeAnmeldelseKlade pSkadeAnmeldelseKlade) {
	  PersistensService.addToCollection(this, "SkadeAnmeldelseKlade", pSkadeAnmeldelseKlade);
  }
  public void removeSkadeAnmeldelseKlade(SkadeAnmeldelseKlade oldSkadeAnmeldelseKlade)  {
	  PersistensService.removeFromCollection(this, "SkadeAnmeldelseKlade", oldSkadeAnmeldelseKlade);
  }

    public boolean isOphoertFoerSenesteStormflodopkraevning(){
        if (!isOphUdfyldt()){
            return false;
        }

		AarsAfgiftStormflodIF stormflod = getNyesteStormflod();
        boolean isOphoert = isOphoertFoerNyesteAarsafgift(stormflod);
        return isOphoert;
    }

    public boolean isOphoertFoerSenesteAarsafgiftopkraevning(){
        if (!isOphUdfyldt()){
            return false;
        }

        AarsAfgiftStormflodIF nyesteAarsafgift = getNyesteAarsafgift();
        boolean isOphoert = isOphoertFoerNyesteAarsafgift(nyesteAarsafgift);
        return isOphoert;
    }

    private boolean isOphoertFoerNyesteAarsafgift(AarsAfgiftStormflodIF pAarsafgift)  {
	    if (pAarsafgift == null)
	        return false;

        Fordring pFordringAarsafgift = pAarsafgift.getFordring();
        if (pFordringAarsafgift == null) {
            return false;
        }
        if (pFordringAarsafgift.getNettobeloeb().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        // HOT-92399 og HOT-92417 -- Vi står midt i Statuskørsel
        // Når Stormfloden lige er skabt skal den ikke ristorneres
        Afregning afregning = pFordringAarsafgift.getAfregning();
        if (afregning == null) {
            return false;
        }
        Fordring[] fordringAlle = afregning.getFordring();
        Arrays.sort(fordringAlle, new FordringPeriodefraOpretdatoTidComparator());
        BigDecimal praemiePeriodeFra = GaiaConst.FIKTIV_DATO_99991231;
        for (int i = fordringAlle.length - 1; i >= 0; i--) {
            if (fordringAlle[i].isPraemieFordring()) {
                if (Datobehandling.getAarFraDato(fordringAlle[i].getPeriodeFra()) == Datobehandling.getAarFraDato(pFordringAarsafgift.getPeriodeFra())) {
                    if (fordringAlle[i].getPeriodeFra().compareTo(praemiePeriodeFra) < 0) {
                        praemiePeriodeFra = fordringAlle[i].getPeriodeFra();
                    }
                }
            }
        }
        if (praemiePeriodeFra.compareTo(GaiaConst.FIKTIV_DATO_99991231) < 0) {
            return getOph().compareTo(praemiePeriodeFra) < 0;
        }
        return false;
    }

    @Override
	public Stormflod getNyesteStormflod() {
		Stormflod[] stormflodTab = this.getStormflod();
		if (stormflodTab != null) {
			Arrays.sort(stormflodTab, new ModelObjektGldOpretdatoTidComparator());
			return stormflodTab[stormflodTab.length - 1];
		}
		return null;
	}

	@Override
	public AftaleAarsAfgiftFordring getNyesteAarsafgift() {
        final AftaleAarsAfgiftFordring[] aftaleAarsAfgiftFordring = getAftaleAarsAfgiftFordring();
		if (aftaleAarsAfgiftFordring != null) {
			Arrays.sort(aftaleAarsAfgiftFordring, new ModelObjektGldOpretdatoTidComparator());
			return aftaleAarsAfgiftFordring[aftaleAarsAfgiftFordring.length - 1];
		}
		return null;
	}

	@Override
	public void setFornyelsesUdstedelsesaarsagHvisIngenAnden(BigDecimal pFornyelsesdato) {
		AftaleAfegn[] aftaleAfegnTab = getAftaleAfegn(AftaleegngrpImpl.getEgenskabKortnavn(Aftaleegngrp.UDSTAARSAG));
		if (aftaleAfegnTab != null) {
			for (AftaleAfegn aftaleAfegn : aftaleAfegnTab) {
				if (aftaleAfegn.getGld().compareTo(pFornyelsesdato) == 0) {
					return;
				}
			}
		}
		AftaleEgenskabSystem aftaleEgenskabSystem = new AftaleEgenskabSystem(this, pFornyelsesdato);
		String udstedelsesAarsag = ((new DBServerUtil()).isSelskabPresent(Selskaber.BO) ?
									UdstedelsesAarsag.AARLIG_INDEXERING.getUdstedelsesAarsag() :
									UdstedelsesAarsag.FORNYELSE.getUdstedelsesAarsag());
		aftaleEgenskabSystem.opdater(Aftaleegngrp.UDSTAARSAG, udstedelsesAarsag);
		aftaleEgenskabSystem.opdater(Aftaleegngrp.FORSIKRING_AENDRET_PR, Datobehandling.format(pFornyelsesdato));
		boolean harSelvStartetTransaction = PersistensService.transactionBegin();
		aftaleEgenskabSystem.gem();
		if (harSelvStartetTransaction) {
			PersistensService.transactionCommit();
		}
	}

	/** Giver datoen for sidste rettelse af et tilbud */
	@Override
	public String getSidstRettet() {
		String sidsteRettet = this.getRev().trim();
		if (sidsteRettet.equals("")) {
			sidsteRettet = this.getOpr().trim();
		}

		final AftaleStatustype aftaleStatustypeNyesteUdenOphoer = this.getAftaleStatustypeNyesteUdenOphoer();
		if (aftaleStatustypeNyesteUdenOphoer.getStatustype().getKortBenaevnelse().equals(Statustype.STATUS_TYPE_TILBUD_UDL)) {
			sidsteRettet = aftaleStatustypeNyesteUdenOphoer.getOpr();
		} else {
			//  27376 kunne ikke få afgjort om ovenstående status Udløbet skulle forfines med ophørsstatustyper eller gælde generelt.
			// Nu vil et andet statusskift efter seneste revdato vinde - nok/forhåbentlig ikke afgørende.
			BigDecimal datoen = this.getRevOprDato();
			BigDecimal statusdato = aftaleStatustypeNyesteUdenOphoer.getOprDato();
			if (statusdato.compareTo(datoen) > 0)
				sidsteRettet = aftaleStatustypeNyesteUdenOphoer.getOpr();
		}
		return sidsteRettet;
	}

	@Override
	public boolean isPanthaverFlyttetMed(PanthaverBO pPanthaverBO) {
		AftaleFlytningLog[] aftaleFlytningLogFraAftale = this.getAftaleFlytningLogFraAftale();
		if (aftaleFlytningLogFraAftale != null && aftaleFlytningLogFraAftale.length > 0) {
			Aftale tilAftale = aftaleFlytningLogFraAftale[0].getTilAftale();
			ArrayList<Genstand> genstandeGld = tilAftale.getGenstandeGld(tilAftale.getGld());
			for (Genstand genstand : genstandeGld) {
				InGnAf[] inGnAfTab = genstand.getInGnAf(tilAftale.getGld());
				if (inGnAfTab != null) {
					for (InGnAf inGnAf : inGnAfTab) {
						if (inGnAf.getIndivid().equals(pPanthaverBO.getInGnAf_().getIndivid()) &&
							inGnAf.getAftale().equals(pPanthaverBO.getInGnAf_().getAftale())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isUlykke() {
		AftaletypeImpl aftaleTypen = getAftaleTypen();
		if (aftaleTypen != null) {
			String kortBenaevnelse = aftaleTypen.getKortBenaevnelse();
			if (kortBenaevnelse.equalsIgnoreCase("ULY") || kortBenaevnelse.equalsIgnoreCase("ULD")) {
				return true;
			}
		}
		return false;
	}

	public String getProvisionsModtagerPrimaerToDisplay(BigDecimal dato) {
		String provisionsModtagerPrimaerToDisplay = null;
		Aftalehaendelse aftalehaendelseNyeste = getAftalehaendelseNyeste(dato, true);
		if (aftalehaendelseNyeste != null && aftalehaendelseNyeste.getProvisionsModtagerPrimaer() != null) {
			provisionsModtagerPrimaerToDisplay = aftalehaendelseNyeste.getProvisionsModtagerPrimaerToDisplay();
		} else {
			aftalehaendelseNyeste = getAftalehaendelseNyesteMedProvisionsmodtager(dato);
			if (aftalehaendelseNyeste != null) {
				provisionsModtagerPrimaerToDisplay = aftalehaendelseNyeste.getProvisionsModtagerPrimaerToDisplay();
			}
		}
		return provisionsModtagerPrimaerToDisplay;
	}

}