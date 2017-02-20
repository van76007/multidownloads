package dev.multidownloads;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.multidownloads.builder.CatalogBuilder;
import dev.multidownloads.builder.InforBuilder;
import dev.multidownloads.model.DownloadCatalog;
import dev.multidownloads.model.DownloadInfor;
import dev.multidownloads.model.DownloadTask;
import dev.multidownloads.model.Segmentation;
import junit.framework.TestCase;

/**
 * Unit test for CatalogBuilder class
 * 
 * @author vanvu
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogBuilderTest extends TestCase {
	private static final int FILE_LEN = 262144 * 10;
	private CatalogBuilder catalogBuilder;

	InforBuilder inforBuilder1;
	InforBuilder inforBuilder2;

	@Before
	public void setUp() {
		inforBuilder1 = Mockito.spy(new InforBuilder());
		Mockito.when(inforBuilder1.buildInfors(ArgumentMatchers.<String>anyList(), Mockito.anyString()))
				.thenReturn(buidMockInfors1());

		inforBuilder2 = Mockito.spy(new InforBuilder());
		Mockito.when(inforBuilder2.buildInfors(ArgumentMatchers.<String>anyList(), Mockito.anyString()))
				.thenReturn(buidMockInfors2());

		catalogBuilder = new CatalogBuilder();
	}

	@Test
	public void testDownloadTaskFileSizesShouldBeInAscendingOrder() {
		catalogBuilder.setBuilder(inforBuilder1);

		DownloadCatalog catalog = new DownloadCatalog();
		catalogBuilder.buildCatalog(catalog, "");

		List<DownloadTask> tasks = catalog.getTasks();
		assertTrue(tasks.size() == 2);

		DownloadTask prev = null;
		for (DownloadTask task : tasks) {
			if (prev != null) {
				assertNotNull(prev.getInfor());
				assertNotNull(task.getInfor());
				assertTrue(prev.getInfor().getFileLength() <= task.getInfor().getFileLength());
			}
			prev = task;
		}
	}

	@Test
	public void testSegmentationsShouldHaveOneSegIfNotSupportMultiPartsDownload() {
		catalogBuilder.setBuilder(inforBuilder1);

		DownloadCatalog catalog = new DownloadCatalog();
		catalogBuilder.buildCatalog(catalog, "");

		assertTrue(catalog.getTasks().size() == 2);

		List<DownloadTask> tasks = catalog.getTasks();
		assertTrue(tasks.size() == 2);

		for (DownloadTask task : tasks) {
			assertNotNull(task.getInfor());
			if (!task.getInfor().isSupportMultiPartsDownload()) {
				assertTrue(task.getSegmentations().size() == 1);
			}
		}
	}

	@Test
	public void testSegmentationsShouldNotOverlap() {
		catalogBuilder.setBuilder(inforBuilder2);

		DownloadCatalog catalog = new DownloadCatalog();
		catalogBuilder.buildCatalog(catalog, "");

		assertTrue(catalog.getTasks().size() == 1);

		DownloadTask task = catalog.getTasks().get(0);
		List<Segmentation> segs = task.getSegmentations();
		Segmentation prev = null;
		for (Segmentation seg : segs) {
			if (prev != null) {
				assertEquals(seg.startByte, prev.endByte + 1);
			}
			prev = seg;
		}

		assertEquals(prev.endByte, FILE_LEN - 1);
	}

	private List<DownloadInfor> buidMockInfors1() {
		DownloadInfor infor1 = new DownloadInfor();
		infor1.setFileLength(FILE_LEN);
		infor1.setSupportMultiPartsDownload(true);

		DownloadInfor infor2 = new DownloadInfor();
		infor2.setFileLength(FILE_LEN / 2);
		infor2.setSupportMultiPartsDownload(false);

		return Arrays.asList(infor1, infor2);
	}

	private List<DownloadInfor> buidMockInfors2() {
		DownloadInfor infor = new DownloadInfor();
		infor.setFileLength(FILE_LEN);
		infor.setSupportMultiPartsDownload(true);
		return Arrays.asList(infor);
	}
}
