package br.com.celodemelo.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class LessServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Map<String, String> compiledFiles = new HashMap<String, String>();
	private LessEngine engine;
	private boolean compress;

	@Override
	public void init(ServletConfig config) throws ServletException {
		engine = new LessEngine();
		compress = Boolean.parseBoolean(config.getInitParameter("compress"));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String resource = req.getServletPath();

		if (resource.endsWith(".css")) {

			try {

				String css = compiledFiles.get(resource);

				if (css == null) {
					URL url = getCssURL(req);
					css = engine.compile(url);
					compiledFiles.put(resource, css);
				}

				if (compress){
					css = compress(css);
				}
				
				resp.setContentType("text/css");
				resp.getWriter().write(css);

			} catch (LessException e) {
				throw new ServletException(e.getMessage(), e);
			}

		}

	}

	private String compress(String content) throws IOException {
		
		Reader in = new InputStreamReader(new ByteArrayInputStream((new String(content.getBytes(), "UTF-8")).replaceFirst("^/\\*", "/*!").getBytes("UTF-8")), "UTF-8");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(baos, "UTF-8");
		
		CssCompressor compressor = new CssCompressor(in);
		in.close();
		compressor.compress(out, -1);
		out.flush();
		String compressed = new String(baos.toByteArray());
		out.close();
		
		return compressed;
	}

	private URL getCssURL(HttpServletRequest req) throws MalformedURLException {
		String path = req.getServletContext().getRealPath(req.getServletPath().replaceAll("\\.css", ".less"));
		return new File(path).toURI().toURL();
	}

}
