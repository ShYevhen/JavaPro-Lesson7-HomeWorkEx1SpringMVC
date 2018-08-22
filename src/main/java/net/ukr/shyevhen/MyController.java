package net.ukr.shyevhen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.zip.ZipHeaders;
import org.springframework.integration.zip.transformer.ZipTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class MyController {
	@Autowired
	private ZipTransformer zip;
	private static final int ITEMS_PER_PAGE = 10;
	private Map<Long, byte[]> photos = new HashMap<Long, byte[]>();

	@RequestMapping("/")
	public String onIndex() {
		return "index";
	}

	@RequestMapping(value = "/add_photo", method = RequestMethod.POST)
	public String onAddPhoto(Model model, @RequestParam MultipartFile photo) {
		if (photo.isEmpty())
			throw new PhotoErrorException();

		try {
			long id = System.currentTimeMillis();
			photos.put(id, photo.getBytes());

			model.addAttribute("photo_id", id);
			return "result";
		} catch (IOException e) {
			throw new PhotoErrorException();
		}
	}

	@RequestMapping("/photo/{photo_id}")
	public ResponseEntity<byte[]> onPhoto(@PathVariable("photo_id") long id) {
		return photoById(id);
	}

	@RequestMapping(value = "/view", method = RequestMethod.POST)
	public ResponseEntity<byte[]> onView(@RequestParam("photo_id") long id) {
		return photoById(id);
	}

	@RequestMapping("/delete/{photo_id}")
	public String onDelete(@PathVariable("photo_id") long id) {
		if (photos.remove(id) == null)
			throw new PhotoNotFoundException();
		else
			return "index";
	}

	@RequestMapping("/view_all")
	public String getAllPhoto(Model model, @RequestParam(required = false, defaultValue = "0") Integer page) {
		if (page < 0) {
			page = 0;
		}
		long totalCount = photos.size();
		int start = page * ITEMS_PER_PAGE;
		long pageCount = (totalCount / ITEMS_PER_PAGE) + ((totalCount % ITEMS_PER_PAGE > 0) ? 1 : 0);

		model.addAttribute("photos", listPhotos(start));
		model.addAttribute("pages", pageCount);
		return "view";
	}

	@RequestMapping(value = "/delete_images", method = RequestMethod.POST)
	public ResponseEntity<Void> delete(@RequestParam(value = "toDelete[]", required = false) long[] toDelete) {
		if (toDelete != null && toDelete.length > 0) {
			for (int i = 0; i < toDelete.length; i += 1) {
				photos.remove(toDelete[i]);
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
//	@RequestMapping(value = "/download_images", method = RequestMethod.POST)
//	public void download(@RequestParam(value = "toDo[]", required = false) long[] toDownloads, HttpServletResponse resp) {
//		Date date = new Date();
//		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy.HHmmss");
//		File file = new File(sdf.format(date) + ".zip");
//		if (toDownload != null && toDownload.length > 0) {
//			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
//				for (int i = 0; i < toDownload.length; i += 1) {
//					ZipEntry entry = new ZipEntry("image" + i + ".jpeg");
//					out.putNextEntry(entry);
//					out.write(photos.get(toDownload[i]));
//					out.closeEntry();
//				}
//			} catch (IOException e) {
//				System.out.println(e);
//			}
//			try {
//				InputStream inputStream = new FileInputStream(file);
//				resp.setContentType("application/force-download");
//				resp.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
//				IOUtils.copy(inputStream, resp.getOutputStream());
//				resp.flushBuffer();
//				inputStream.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	@RequestMapping(value = "/download_images", method = RequestMethod.POST)
	public void download(@RequestParam(value = "toDo[]", required = false) long[] toDownload, HttpServletResponse resp) {
		if (toDownload != null && toDownload.length > 0) {
			List<byte[]> images = new ArrayList<>();
			for (int i = 0; i < toDownload.length; i += 1) {
				images.add(photos.get(toDownload[i]));
			}
			Message<List<byte[]>> message = MessageBuilder.withPayload(images)
					 .setHeader(ZipHeaders.ZIP_ENTRY_FILE_NAME, "image.jpeg")
					 .setHeader(ZipHeaders.ZIP_ENTRY_LAST_MODIFIED_DATE, new Date())
					 .build();
			final Message<?> result = zip.transform(message);
			sendFile(resp, result);		
		}else {
			try {
				resp.sendRedirect("view_all");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendFile(HttpServletResponse resp, Message<?> result) {
		File file = (File)result.getPayload();
		try {
	        InputStream inputStream = new FileInputStream(file);
	        resp.setContentType("application/force-download");
	        resp.setHeader("Content-Disposition", "attachment; filename="+file.getName()); 
	        IOUtils.copy(inputStream, resp.getOutputStream());
	        resp.flushBuffer();
	        inputStream.close();
	    } catch (Exception e){
	        e.printStackTrace();
	    }finally {
	    	file.delete();
	    }
	}
	
	private ResponseEntity<byte[]> photoById(long id) {
		byte[] bytes = photos.get(id);
		if (bytes == null)
			throw new PhotoNotFoundException();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);

		return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
	}

	private Set<Long> listPhotos(int start) {
		Set<Long> id = photos.keySet();
		Set<Long> page = new HashSet<>();
		int i = 0;
		for (Long l : id) {
			if (i < start || i >= start + ITEMS_PER_PAGE) {
				i += 1;
				continue;
			}
			page.add(l);
			i += 1;
		}
		return page;
	}
}
