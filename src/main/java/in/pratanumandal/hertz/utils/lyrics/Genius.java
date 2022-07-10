package in.pratanumandal.hertz.utils.lyrics;

import core.GLA;
import genius.SongSearch;

import java.io.IOException;
import java.util.List;

public final class Genius {

    public static String search(String query) {
        GLA gla = new GLA();
        try {
            SongSearch search = gla.search(query);

            if (search.getStatus() == 200) {
                List<SongSearch.Hit> hits = search.getHits();

                if (hits.size() > 0) {
                    return hits.get(0).fetchLyrics();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
