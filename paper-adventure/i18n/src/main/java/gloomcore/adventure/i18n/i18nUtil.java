package gloomcore.adventure.i18n;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public class i18nUtil {
    public static final Set<String> ALL_LANG = Set.of(
            "af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "be_latn",
            "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk",
            "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz",
            "en_pt", "en_ud", "en_us", "enp", "enws", "eo_uy", "es_ar", "es_cl",
            "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es",
            "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fur_it",
            "fy_nl", "ga_ie", "gd_gb", "gl_es", "haw_us", "he_il", "hi_in", "hn_no",
            "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv",
            "it_it", "ja_jp", "jbo_en", "ka_ge", "kk_kz", "kn_in", "ko_kr", "ksh",
            "kw_gb", "ky_kg", "la_la", "lb_lu", "li_li", "lmo", "lo_la", "lol_us", "lt_lt",
            "lv_lv", "lzh", "mk_mk", "mn_mn", "ms_my", "mt_mt", "nah", "nds_de",
            "nl_be", "nl_nl", "nn_no", "no_no", "oc_fr", "ovd", "pl_pl", "pls",
            "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "ry_ua", "sah_sah",
            "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_cs", "sr_sp", "sv_se",
            "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tok", "tr_tr",
            "tt_ru", "tzo_mx", "uk_ua", "val_es", "vec_it", "vi_vn", "vp_vl", "yi_de",
            "yo_ng", "zh_cn", "zh_hk", "zh_tw", "zlm_arab"
    );
    private static final Interner<Locale> INTERNER = Interners.newWeakInterner();
    private static final Object2ObjectMap<String, Locale> AVAILABLE_LOCALES;

    static {
        Object2ObjectOpenHashMap<String, Locale> tempMap = new Object2ObjectOpenHashMap<>();
        for (String langCode : ALL_LANG) {
            Locale locale = Locale.forLanguageTag(langCode.replace('_', '-'));
            tempMap.put(langCode, INTERNER.intern(locale));
        }
        AVAILABLE_LOCALES = tempMap;
    }

    public static Locale normalize(final String langCode) {
        return AVAILABLE_LOCALES.get(langCode);
    }

    public static Set<String> getAvailableLangCodes() {
        return AVAILABLE_LOCALES.keySet();
    }

    public static Collection<Locale> getAvailableLocales() {
        return AVAILABLE_LOCALES.values();
    }

    public static Locale intern(Locale locale) {
        if (locale == null) {
            return null;
        }
        return INTERNER.intern(locale);
    }
}