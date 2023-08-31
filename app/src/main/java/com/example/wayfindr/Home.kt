package com.example.wayfindr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.home.ImageSliderAdapter
import com.example.wayfindr.home.ItemClickListener
import com.example.wayfindr.home.home_places_fragment
import com.google.android.material.bottomsheet.BottomSheetDialog


class Home : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageSliderAdapter
    private lateinit var dialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val imageList = listOf(
            R.drawable.galatakulesi, R.drawable.dolmabahce1, R.drawable.arkeolojimuzesi,
            R.drawable.kizkulesi, R.drawable.topkapi_sarayi, R.drawable.rahmi_koc,
            R.drawable.kariye_image, R.drawable.istanbul_modern_image, R.drawable.ural_ataman_image,
            R.drawable.sakip_sabanci_image, R.drawable.masumiyet_image, R.drawable.turkveislam_image
        )
        val captionList = listOf(
            "Galata Kulesi", "Dolmabahçe Sarayı", "İstanbul Arkeoloji Müzesi", "Kız Kulesi",
            "İstanbul Topkapı Sarayı", "Rahmi M. Koç Müzesi", "Kariye Müzesi", "İstanbul Modern Sanat Müzesi",
            "Ural Ataman Klasik Otomobil Müzesi", "Sakıp Sabancı Müzesi", "Masumiyet Müzesi", "Türk ve İslam Eserleri Müzesi"
        )

        val randomItemIndices = getRandomItems(imageList.size, 5)
        val randomImages = randomItemIndices.map { imageList[it] }
        val randomCaptions = randomItemIndices.map { captionList[it] }

        val itemClickListener = object : ItemClickListener {
            override fun onItemClick(position: Int) {
                val clickedCaption = randomCaptions[position]
                showBottomSheetDialog(clickedCaption)
            }
        }

        adapter = ImageSliderAdapter(randomImages, randomCaptions, itemClickListener)
        recyclerView.adapter = adapter

        return rootView
    }

    private fun getRandomItems(totalItems: Int, itemCount: Int): List<Int> {
        val shuffledIndices = (0 until totalItems).shuffled()
        return shuffledIndices.take(itemCount)
    }

    private fun showBottomSheetDialog(clickedCaption: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.home_places_fragment, null)

        val captionTextView = bottomSheetView.findViewById<TextView>(R.id.placesname)
        captionTextView.text = clickedCaption


        val descriptionTextView = bottomSheetView.findViewById<TextView>(R.id.textView4)
        val imageResource = getImageResourceByCaption(clickedCaption)
        val description = getDescriptionByCaption(clickedCaption)

        descriptionTextView.text = description

        val imageView = bottomSheetView.findViewById<ImageView>(R.id.places_image)
        imageView.setImageResource(imageResource)

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun getDescriptionByCaption(caption: String): String {
        return when (caption) {
            "Galata Kulesi" -> "Galata Kulesi ya da müze olarak kullanılmaya başlaması sonrasındaki adıyla Galata Kulesi Müzesi, İstanbul'un Beyoğlu ilçesinde bulunan bir kuledir. Adını, bulunduğu Galata semtinden alır. Galata Surları dahilinde bir gözetleme kulesi olarak inşa edilen kule, farklı dönemlerde farklı amaçlarla kullanılmasının ardından 2020'den itibaren, bir sergi mekânı ve müze olarak hizmet verir. Hem Beyoğlu'nun hem de İstanbul'un sembol yapılarındandır."
            "Dolmabahçe Sarayı" -> "Dolmabahçe Sarayı, İstanbul Boğazı'nın kıyısında yer alan göz alıcı bir yapıdır. Osmanlı İmparatorluğu'nun son dönemlerinde inşa edilen saray, Avrupa etkisindeki neoklasik ve barok tarzlarındaki mimarisiyle öne çıkar. Muhteşem detayları, zarif salonları ve büyüleyici bahçeleriyle Dolmabahçe Sarayı, dönemin ihtişamını yansıtan bir şaheserdir. Tarih sahnesindeki büyük toplantılara, devlet işlerine ve ziyaretlere ev sahipliği yapmıştır. Günümüzde müze olarak kullanılan saray, ziyaretçilerini Osmanlı İmparatorluğu'nun son dönemine götürerek tarihi bir yolculuğa davet eder."
            "İstanbul Arkeoloji Müzesi" -> "İstanbul Arkeoloji Müzesi, zengin koleksiyonuyla tarihin derinliklerine açılan bir penceredir. İstanbul'un Sultanahmet bölgesinde yer alan bu müze, antik çağlardan orta çağa kadar uzanan büyüleyici eserleriyle ünlüdür. Üç ayrı bina ve koleksiyonuyla müze, binlerce yıl öncesine ait eserleri sergiler. Antik dönemin heybetli heykelleri, yazılı belgeleri ve sanat eserleri burada ziyaretçilere görsel bir şölen sunar. Tarihin izlerini taşıyan İstanbul Arkeoloji Müzesi, kültürel mirasımızın bir parçasını keşfetmek isteyenleri ağırlar."
            "Kız Kulesi" -> "Kız Kulesi, İstanbul Boğazı'nda yer alan tarihi ve sembolik bir yapıdır. Efsanelere ve tarihe ev sahipliği yapan bu küçük ada üzerinde yükselen kule, Bizans döneminden günümüze uzanan zengin bir geçmişe sahiptir. Günümüzde restoran ve kafe olarak kullanılan Kız Kulesi, İstanbul'un panoramik güzelliklerini sunan eşsiz bir mekan olarak ziyaretçileri ağırlamaktadır. Boğaz'ın incisi olarak da anılan bu tarihi yapının etkileyici silueti, İstanbul'un simgelerinden biridir ve şehrin zengin kültürel dokusunun bir yansıması olarak öne çıkar."
            "İstanbul Topkapı Sarayı" -> "İstanbul Topkapı Sarayı, Osmanlı İmparatorluğu'nun görkemli ve stratejik merkezlerinden biridir. Boğaz'ın muhteşem manzarasını kucaklayan saray, Osmanlı sultanlarının hükmettiği yerdir. İhtişamlı avluları, zengin iç dekorasyonu ve kıymetli koleksiyonları ile dikkat çeker. Padişahların hayatını yansıtan harem bölümü, kutsal emanetlerin sergilendiği kutsal eşyalar bölümü gibi bölümleriyle tarihle iç içe bir deneyim sunar. Topkapı Sarayı'nın hikayeleri, hem Osmanlı İmparatorluğu'nun zirvesindeki gücü hem de yaşanan hayatları anlatır. Günümüzde müze olarak kullanılan saray, ziyaretçilerini büyülü bir tarihi yolculuğa davet eder."
            "Rahmi M. Koç Müzesi" -> "Rahmi M. Koç Müzesi, zengin koleksiyonuyla sanat, teknoloji ve tarih tutkunlarını büyüleyen bir noktadır. İstanbul'un Hasköy semtinde yer alan bu müze, endüstriyel mirası ve teknolojik ilerlemeyi bir araya getirir. İşte bu noktada, antika otomobillerden gemilere, havacılık araçlarından iletişim teknolojilerine kadar geniş bir yelpazedeki eserlerle zenginleşen müze, ziyaretçilere görsel ve eğitici bir deneyim sunar. Rahmi M. Koç Müzesi, geçmişin izlerini taşıyan koleksiyonuyla tarihin ve teknolojinin gelişimini keşfetmek isteyenleri ağırlar."
            "Kariye Müzesi" -> "Kariye Müzesi, İstanbul'un önemli kültürel hazinelerinden biridir. Bizans dönemine ait tarihi Kariye Camii'nin içinde bulunan bu müze, eşsiz mozaikleri ve freskleriyle ünlüdür. İç mekandaki sanat eserleri, Bizans İmparatorluğu'nun estetik ve dini yaşantısını yansıtır. Renkli mozaikler ve duvar resimleri, Hristiyan sanatının zarafetini ve derinliğini gösterir. Kariye Müzesi, ziyaretçilerini geçmişin mistik atmosferine götürerek tarihi ve sanatsal bir yolculuğa çıkarır. İstanbul'un zengin kültürel dokusunu anlamak ve Bizans dönemine dair izleri görmek isteyenler için unutulmaz bir deneyim sunar."
            "İstanbul Modern Sanat Müzesi" -> "İstanbul Modern Sanat Müzesi, İstanbul'un sanatsal canlılığını yansıtan önemli bir mekandır. Boğaz'ın kıyısında yer alan bu müze, çağdaş sanatın zengin ve çeşitli koleksiyonlarına ev sahipliği yapar. Türk ve uluslararası sanatçıların eserlerini barındıran müze, modern sanatın farklı yönlerini keşfetmek isteyenleri cezbetmektedir. Heykel, resim, fotoğraf, video gibi farklı sanat disiplinlerinin bir arada sergilendiği müze, yaratıcılığı ve düşünsel derinliği kutlar. İstanbul Modern Sanat Müzesi, sanatseverleri çağdaş sanatın dinamik dünyasına davet ederken, şehrin kültürel zenginliğini vurgular."
            "Ural Ataman Klasik Otomobil Müzesi" -> "Ural Ataman Klasik Otomobil Müzesi, otomobil tutkunlarını geçmişin otomobil mirasına götüren özel bir noktadır. İstanbul'da yer alan bu müze, klasik ve nadir otomobillerin koleksiyonunu büyüleyici bir şekilde sergiler. Her bir araç, geçmişin tasarım estetiğini ve mühendislik harikasını yansıtır. Müze, otomobil endüstrisinin evrimini ve tarihinin ilgi çekici bir kesitini sunar. Ziyaretçiler, vintage otomobillerin detaylarını inceleyebilir ve otomobil tarihine dair keyifli bir yolculuk yapabilir. Ural Ataman Klasik Otomobil Müzesi, otomobil meraklılarını nostaljik bir yolculuğa davet ederken, teknolojik gelişmelerin de izini sürmelerine olanak tanır."
            "Sakıp Sabancı Müzesi" -> "Sakıp Sabancı Müzesi, sanat ve kültürün buluştuğu özel bir mekandır. İstanbul'un Emirgan semtinde yer alan bu müze, geniş ve çeşitli koleksiyonlarıyla dikkat çeker. Türk ve uluslararası sanatın farklı dönemlerine ait eserler burada sergilenir. Heykel, resim, seramik, çağdaş sanat gibi çeşitli sanat dallarını barındıran müze, sanatseverlerin ilgisini çeker. Müze binası, tarihi ve zarif bir atmosferde sanat eserlerini ağırlar. Sakıp Sabancı Müzesi, ziyaretçilere kültürel bir yolculuk ve sanatsal bir deneyim sunarak, şehrin zengin sanat mirasını kutlar."
            "Masumiyet Müzesi" -> "Masumiyet Müzesi, İstanbul'da duygusal ve tarihsel bir deneyim sunan özel bir mekandır. Türk yazar Orhan Pamuk'un aynı adlı romanından esinlenerek oluşturulan müze, romanın karakterlerinin hayatlarını ve İstanbul'un dokusunu yansıtan bir atmosfer sunar. Müze, hayatın güzellikleri ve karmaşıklıklarına dair derinlemesine bir yolculuğa davet eder. Ziyaretçiler, karakterlerin yaşadığı mekanları ve hikayeleri keşfederken, romanın iç dünyasına dalabilir. Masumiyet Müzesi, duygu yüklü bir deneyim sunarak edebiyatın gücünü hissetmek isteyenlere ilham verir."
            "Türk ve İslam Eserleri Müzesi" -> "Türk ve İslam Eserleri Müzesi, İstanbul'un tarihi ve kültürel mirasını yansıtan önemli bir noktadır. Sultanahmet Camii'nin yanında yer alan bu müze, Osmanlı ve İslam sanatının zengin koleksiyonlarına ev sahipliği yapar. El yazmaları, minyatürler, seramikler, halılar, cam eserleri gibi çeşitli sanat eserlerini barındıran müze, İslam kültürünün estetik ve dini yönlerini yansıtır. Osmanlı İmparatorluğu'nun zengin tarihini ve sanatsal çeşitliliğini yansıtan müze, ziyaretçileri geçmişin izlerini takip etmeye davet eder. Türk ve İslam Eserleri Müzesi, tarih ve sanat meraklıları için derinlemesine bir deneyim sunar ve İstanbul'un zengin kültürel geçmişine ışık tutar."
            else -> "Açıklama bulunamadı."
        }
    }

    private fun getImageResourceByCaption(caption: String): Int {
        return when (caption) {
            "Galata Kulesi" -> R.drawable.galata_kulesi
            "Dolmabahçe Sarayı" -> R.drawable.dolmabahce1
            "İstanbul Arkeoloji Müzesi" -> R.drawable.arkeolojimuzesi
            "Kız Kulesi" -> R.drawable.kizkulesi
            "İstanbul Topkapı Sarayı" -> R.drawable.topkapi_sarayi
            "Rahmi M. Koç Müzesi" -> R.drawable.rahmi_koc
            "Kariye Müzesi" -> R.drawable.kariye_image
            "İstanbul Modern Sanat Müzesi" -> R.drawable.istanbul_modern_image
            "Ural Ataman Klasik Otomobil Müzesi" -> R.drawable.ural_ataman_image
            "Sakıp Sabancı Müzesi" -> R.drawable.sakip_sabanci_image
            "Masumiyet Müzesi" -> R.drawable.masumiyet_image
            "Türk ve İslam Eserleri Müzesi" -> R.drawable.islam_bilim_image
            else -> R.drawable.baseline_person_24
        }
    }



}

